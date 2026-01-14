package br.com.carbigdata.ocorrencias_reactive.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OcorrenciaDTO {

    @NotBlank
    private String nmeCliente;

    @NotNull
    private LocalDate dtaNascimento;

    @NotBlank
    private String nroCpf;

    @NotBlank
    private String nmeLogradouro;

    private String nmeBairro;

    @NotBlank
    private String nroCep;

    @NotBlank
    private String nmeCidade;

    @NotBlank
    private String nmeEstado;

    @NotNull
    private LocalDateTime dtaOcorrencia;
}