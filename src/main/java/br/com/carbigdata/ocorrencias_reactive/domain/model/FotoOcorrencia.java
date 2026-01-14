package br.com.carbigdata.ocorrencias_reactive.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FotoOcorrencia(
        Long codFotoOcorrencia,
        Long codOcorrencia,
        LocalDateTime dtaCriacao,
        String dscPathBucket,
        String dscHash
) {}