package br.com.carbigdata.ocorrencias_reactive.domain.model;

import lombok.Builder;

@Builder
public record Endereco(
        Long codEndereco,
        String nmeLogradouro,
        String nmeBairro,
        String nroCep,
        String nmeCidade,
        String nmeEstado
) {}