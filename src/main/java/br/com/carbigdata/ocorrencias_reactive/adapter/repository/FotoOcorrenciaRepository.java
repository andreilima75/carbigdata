package br.com.carbigdata.ocorrencias_reactive.adapter.repository;

import br.com.carbigdata.ocorrencias_reactive.domain.model.FotoOcorrencia;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FotoOcorrenciaRepository extends ReactiveCrudRepository<FotoOcorrencia, Long> {
}