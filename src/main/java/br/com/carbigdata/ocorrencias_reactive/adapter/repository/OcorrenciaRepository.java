package br.com.carbigdata.ocorrencias_reactive.adapter.repository;

import br.com.carbigdata.ocorrencias_reactive.domain.model.Ocorrencia;
import br.com.carbigdata.ocorrencias_reactive.domain.model.StatusOcorrencia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OcorrenciaRepository extends ReactiveCrudRepository<Ocorrencia, Long> {

    Flux<Ocorrencia> findAllBy(Pageable pageable);

    Flux<Ocorrencia> findByStaOcorrenciaOrderByDtaOcorrenciaDesc(
            StatusOcorrencia status, Pageable pageable);

    @Query("SELECT o.* FROM ocorrencia o ORDER BY o.dta_ocorrencia DESC")
    Flux<Ocorrencia> findAllCursor();
}