package br.com.carbigdata.ocorrencias_reactive.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record Ocorrencia(
        Long codOcorrencia,
        Long codCliente,
        Long codEndereco,
        LocalDateTime dtaOcorrencia,
        StatusOcorrencia staOcorrencia
) {}