package br.com.carbigdata.ocorrencias_reactive.adapter.web.dto;


import br.com.carbigdata.ocorrencias_reactive.domain.model.StatusOcorrencia;

import java.time.LocalDateTime;

public record OcorrenciaResponseDTO(
        Long codOcorrencia,
        String nmeCliente,
        String nroCpf,
        String nmeLogradouro,
        String nmeCidade,
        LocalDateTime dtaOcorrencia,
        StatusOcorrencia staOcorrencia,
        String urlFoto
) {}