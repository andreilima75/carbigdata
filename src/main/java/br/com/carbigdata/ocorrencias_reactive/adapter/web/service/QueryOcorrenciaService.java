package br.com.carbigdata.ocorrencias_reactive.adapter.web.service;


import br.com.carbigdata.ocorrencias_reactive.adapter.repository.OcorrenciaRepository;
import br.com.carbigdata.ocorrencias_reactive.adapter.web.dto.OcorrenciaResponseDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class QueryOcorrenciaService {

    private final OcorrenciaRepository repo;

    public QueryOcorrenciaService(OcorrenciaRepository repo) {
        this.repo = repo;
    }

    public Flux<OcorrenciaResponseDTO> listarPaginado(int page, int size, String status) {
        return repo.findAllByOrderByDtaOcorrenciaDesc()
                .skip((long) page * size)
                .take(size)
                .map(o -> new OcorrenciaResponseDTO(o.codOcorrencia(), null, null, null, null, o.dtaOcorrencia(), o.staOcorrencia(), null));
    }

    public Flux<OcorrenciaResponseDTO> streamTodas() {
        return repo.findAllCursor()
                .map(o -> new OcorrenciaResponseDTO(o.codOcorrencia(), null, null, null, null, o.dtaOcorrencia(), o.staOcorrencia(), null));
    }
}