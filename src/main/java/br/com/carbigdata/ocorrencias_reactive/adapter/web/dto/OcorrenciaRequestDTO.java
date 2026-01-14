package br.com.carbigdata.ocorrencias_reactive.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OcorrenciaRequestDTO(
        @NotBlank String nmeCliente,
        @NotNull @PastOrPresent LocalDate dtaNascimento,
        @NotBlank @Pattern(regexp = "\\d{11}") String nroCpf,
        @NotBlank String nmeLogradouro,
        String nmeBairro,
        @NotBlank @Pattern(regexp = "\\d{8}") String nroCep,
        @NotBlank String nmeCidade,
        @NotBlank @Pattern(regexp = "[A-Z]{2}") String nmeEstado,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dtaOcorrencia
) {
}