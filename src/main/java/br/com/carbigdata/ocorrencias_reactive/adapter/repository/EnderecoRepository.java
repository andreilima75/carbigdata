package br.com.carbigdata.ocorrencias_reactive.adapter.repository;

import br.com.carbigdata.ocorrencias_reactive.domain.model.Endereco;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EnderecoRepository extends ReactiveCrudRepository<Endereco, Long> {
}