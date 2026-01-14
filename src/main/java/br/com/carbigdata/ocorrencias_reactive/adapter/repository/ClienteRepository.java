package br.com.carbigdata.ocorrencias_reactive.adapter.repository;

import br.com.carbigdata.ocorrencias_reactive.domain.model.Cliente;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ClienteRepository extends ReactiveCrudRepository<Cliente, Long> {

    @Query("""
            INSERT INTO cliente (nme_cliente, dta_nascimento, nro_cpf)
            VALUES (:nmeCliente, :dtaNascimento, :nroCpf)
            ON CONFLICT (nro_cpf) DO UPDATE SET nme_cliente = EXCLUDED.nme_cliente
            RETURNING cod_cliente
            """)
    Mono<Long> upsertByCpf(String nmeCliente, java.time.LocalDate dtaNascimento, String nroCpf);
}