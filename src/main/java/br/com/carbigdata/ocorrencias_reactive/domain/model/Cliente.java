package br.com.carbigdata.ocorrencias_reactive.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Cliente(
        Long codCliente,
        String nmeCliente,
        LocalDate dtaNascimento,
        String nroCpf,
        LocalDateTime dtaCriacao
) {}