package br.com.carbigdata.ocorrencias_reactive.adapter.web.service;

import br.com.carbigdata.ocorrencias_reactive.adapter.web.dto.OcorrenciaRequestDTO;
import br.com.carbigdata.ocorrencias_reactive.domain.event.OcorrenciaRecebida;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Slf4j
public class IngestaoOcorrenciaService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;
    private final MinioService minioService;

    private static final String EXCHANGE_NAME = "ocorrencias.exchange";
    private static final String ROUTING_KEY = "ocorrencias.recebidas.key";

    private static final String DLQ_ROUTING_KEY = "ocorrencias.recebidas.dlq";

    public IngestaoOcorrenciaService(RabbitTemplate rabbitTemplate, MinioService minioService) {
        this.rabbitTemplate = rabbitTemplate;
        this.minioService = minioService;

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.findAndRegisterModules();

        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("Mensagem confirmada pelo broker. ID: {}",
                        correlationData != null ? correlationData.getId() : "N/A");
            } else {
                log.error("Falha na entrega. Causa: {}", cause);
            }
        });

        this.rabbitTemplate.setReturnsCallback(returned -> {
            log.error("Mensagem não roteável: {}", returned.getMessage());
            log.error("  Exchange: {}", returned.getExchange());
            log.error("  Routing Key: {}", returned.getRoutingKey());
            log.error("  Reply Code: {}", returned.getReplyCode());
            log.error("  Reply Text: {}", returned.getReplyText());

            try {
                String mensagem = new String(returned.getMessage().getBody(), StandardCharsets.UTF_8);
                rabbitTemplate.convertAndSend("ocorrencias.dlx", DLQ_ROUTING_KEY, mensagem);
                log.info("✅ Mensagem enviada para DLQ");
            } catch (Exception e) {
                log.error("Falha ao enviar para DLQ", e);
            }
        });

        log.info("IngestaoOcorrenciaService inicializado. Exchange: '{}', RoutingKey: '{}'",
                EXCHANGE_NAME, ROUTING_KEY);
    }

    public Mono<String> publicarOcorrencia(OcorrenciaRequestDTO dto, FilePart fotoPart) {
        log.info("Iniciando publicação de ocorrência para: {}", dto.nmeCliente());

        return minioService.uploadFileReactive(fotoPart)
                .flatMap(urlFoto -> {
                    try {
                        OcorrenciaRecebida evento = criarEventoOcorrencia(dto, fotoPart, urlFoto);
                        String idMensagem = UUID.randomUUID().toString();

                        String jsonEvento = mapper.writeValueAsString(evento);
                        log.debug("JSON gerado ({} bytes) para CPF: {}",
                                jsonEvento.length(), dto.nroCpf());

                        CorrelationData correlationData = new CorrelationData(idMensagem);

                        return Mono.fromCallable(() -> {
                            log.info("Enviando para RabbitMQ - Exchange: '{}', RoutingKey: '{}', ID: {}",
                                    EXCHANGE_NAME, ROUTING_KEY, correlationData.getId());

                            rabbitTemplate.convertAndSend(
                                    EXCHANGE_NAME,
                                    ROUTING_KEY,
                                    jsonEvento,
                                    message -> {
                                        message.getMessageProperties().setMessageId(idMensagem);
                                        message.getMessageProperties().setContentType("application/json");
                                        message.getMessageProperties().setContentEncoding("UTF-8");
                                        message.getMessageProperties().setHeader("X-CPF", dto.nroCpf());
                                        message.getMessageProperties().setHeader("X-Cliente", dto.nmeCliente());
                                        return message;
                                    },
                                    correlationData
                            );

                            return idMensagem;
                        }).subscribeOn(Schedulers.boundedElastic());

                    } catch (JsonProcessingException e) {
                        log.error("Erro ao serializar evento para JSON", e);
                        return Mono.error(new RuntimeException("Erro ao formatar dados da ocorrência", e));
                    }
                })
                .doOnSuccess(id ->
                        log.info("Processamento concluído. Mensagem ID: {}", id))
                .doOnError(e ->
                        log.error("Erro no processamento para CPF: {}", dto.nroCpf(), e));
    }

    public Mono<Void> publicarOcorrenciaComMono(OcorrenciaRequestDTO dto, Mono<FilePart> fotoPartMono) {
        return fotoPartMono
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "A parte 'foto' é obrigatória")))
                .flatMap(fotoPart -> publicarOcorrencia(dto, fotoPart))
                .then();
    }

    private OcorrenciaRecebida criarEventoOcorrencia(OcorrenciaRequestDTO dto, FilePart fotoPart, String urlFoto) {
        return OcorrenciaRecebida.builder()
                .nmeCliente(dto.nmeCliente())
                .dtaNascimento(dto.dtaNascimento())
                .nroCpf(dto.nroCpf())
                .nmeLogradouro(dto.nmeLogradouro())
                .nmeBairro(dto.nmeBairro())
                .nroCep(dto.nroCep())
                .nmeCidade(dto.nmeCidade())
                .nmeEstado(dto.nmeEstado())
                .dtaOcorrencia(dto.dtaOcorrencia())
                .dscPathBucket(urlFoto)
                .nomeOriginalFoto(fotoPart.filename())
                .contentType(fotoPart.headers().getContentType() != null
                        ? fotoPart.headers().getContentType().toString()
                        : "application/octet-stream")
                .build();
    }

}