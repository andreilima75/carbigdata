package br.com.carbigdata.ocorrencias_reactive.domain.event;

import br.com.carbigdata.ocorrencias_reactive.domain.model.StatusOcorrencia;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record OcorrenciaRecebida(
        String nmeCliente,
        LocalDate dtaNascimento,
        String nroCpf,
        String nmeLogradouro,
        String nmeBairro,
        String nroCep,
        String nmeCidade,
        String nmeEstado,
        LocalDateTime dtaOcorrencia,
        String nomeOriginalFoto,
        String contentType,
        String dscPathBucket,
        StatusOcorrencia statusInicial
) implements DomainEvent {
}