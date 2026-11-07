package br.com.carbigdata.ocorrencias_reactive.adapter.messaging;

import br.com.carbigdata.ocorrencias_reactive.adapter.repository.ClienteRepository;
import br.com.carbigdata.ocorrencias_reactive.adapter.repository.EnderecoRepository;
import br.com.carbigdata.ocorrencias_reactive.adapter.repository.FotoOcorrenciaRepository;
import br.com.carbigdata.ocorrencias_reactive.adapter.repository.OcorrenciaRepository;
import br.com.carbigdata.ocorrencias_reactive.domain.event.OcorrenciaRecebida;
import br.com.carbigdata.ocorrencias_reactive.domain.model.Endereco;
import br.com.carbigdata.ocorrencias_reactive.domain.model.FotoOcorrencia;
import br.com.carbigdata.ocorrencias_reactive.domain.model.Ocorrencia;
import br.com.carbigdata.ocorrencias_reactive.domain.model.StatusOcorrencia;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class OcorrenciaWorker {

    private static final Logger log = LoggerFactory.getLogger(OcorrenciaWorker.class);
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final FotoOcorrenciaRepository fotoOcorrenciaRepository;
    private final TransactionalOperator tx;

    public OcorrenciaWorker(ClienteRepository clienteRepository, EnderecoRepository enderecoRepository, OcorrenciaRepository ocorrenciaRepository, FotoOcorrenciaRepository fotoOcorrenciaRepository, TransactionalOperator tx) {
        this.tx = tx;
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.fotoOcorrenciaRepository = fotoOcorrenciaRepository;
    }


    @RabbitListener(
            queues = "ocorrencias.recebidas",
            concurrency = "5",
            ackMode = "MANUAL"
    )
    public void consumir(org.springframework.amqp.core.Message message,
                         com.rabbitmq.client.Channel channel) {

        Mono.just(message)
                .publishOn(Schedulers.parallel())
                .map(msg -> {
                    try {
                        String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                        return mapper.readValue(json, OcorrenciaRecebida.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro no parse", e);
                    }
                })
                .flatMap(evento -> processarOcorrencia(evento).as(tx::transactional))
                .doOnSuccess(v -> {
                    try {
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                        log.info("ACK enviado para mensagem {}", message.getMessageProperties().getMessageId());
                    } catch (Exception e) {
                        log.error("Erro no ACK", e);
                    }
                })
                .doOnError(err -> {
                    try {
                        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                        log.error("NACK + DLQ: {}", err.getMessage());
                    } catch (Exception e) {
                        log.error("Erro no NACK", e);
                    }
                })
                .onErrorResume(err -> Mono.empty())
                .subscribe();
    }

    private Mono<Void> processarOcorrencia(OcorrenciaRecebida evento) {
        return Mono.zip(
                        clienteRepository.upsertByCpf(
                                evento.nmeCliente(),
                                evento.dtaNascimento(),
                                evento.nroCpf()
                        ),
                        enderecoRepository.save(
                                Endereco.builder()
                                        .codEndereco(null)
                                        .nroCep(evento.nroCep())
                                        .nmeEstado(evento.nmeEstado())
                                        .nmeBairro(evento.nmeBairro())
                                        .nmeCidade(evento.nmeCidade())
                                        .nmeLogradouro(evento.nmeLogradouro())
                                        .build()
                        )
                )
                .flatMap(tuple -> {
                    Long codCliente = tuple.getT1();
                    Endereco enderecoSalvo = tuple.getT2();

                    return ocorrenciaRepository.save(
                                    Ocorrencia.builder()
                                            .codOcorrencia(null)
                                            .codCliente(codCliente)
                                            .codEndereco(enderecoSalvo.codEndereco())
                                            .dtaOcorrencia(evento.dtaOcorrencia())
                                            .staOcorrencia(StatusOcorrencia.ATIVA)
                                            .build()
                            )
                            .flatMap(ocorrenciaSalva -> {
                                FotoOcorrencia foto =
                                        FotoOcorrencia.builder()
                                                .codFotoOcorrencia(null)
                                                .codOcorrencia(ocorrenciaSalva.codOcorrencia())
                                                .dtaCriacao(null)
                                                .dscPathBucket(evento.dscPathBucket())
                                                .dscHash(null)
                                                .build();

                                return fotoOcorrenciaRepository.save(foto)
                                        .thenReturn(ocorrenciaSalva);
                            });
                })
                .doOnSuccess(ocorrenciaSalva -> log.info("Ocorrência {} processada com sucesso! Foto: {}",
                        evento.nroCpf(), evento.dscPathBucket()))
                .doOnError(err -> log.error("Erro ao processar CPF {}: {}", evento.nroCpf(), err.getMessage()))
                .then();
    }
}