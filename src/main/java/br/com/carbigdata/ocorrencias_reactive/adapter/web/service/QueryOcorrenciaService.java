package br.com.carbigdata.ocorrencias_reactive.adapter.web.service;

import br.com.carbigdata.ocorrencias_reactive.adapter.repository.OcorrenciaRepository;
import br.com.carbigdata.ocorrencias_reactive.adapter.web.dto.OcorrenciaResponseDTO;
import br.com.carbigdata.ocorrencias_reactive.domain.model.Ocorrencia;
import br.com.carbigdata.ocorrencias_reactive.domain.model.StatusOcorrencia;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

@Service
public class QueryOcorrenciaService {

    private static final int MAX_PAGE_SIZE = 100;

    private final OcorrenciaRepository repo;

    public QueryOcorrenciaService(OcorrenciaRepository repo) {
        this.repo = repo;
    }

    /**
     * Lista ocorrências paginando no banco (LIMIT/OFFSET), da mais recente para a mais antiga.
     * Se {@code status} for informado, filtra por ele (ATIVA ou FINALIZADA).
     */
    public Flux<OcorrenciaResponseDTO> listarPaginado(int page, int size, String status) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page deve ser >= 0 e size deve estar entre 1 e " + MAX_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dtaOcorrencia"));

        Flux<Ocorrencia> ocorrencias = (status == null || status.isBlank())
                ? repo.findAllBy(pageable)
                : repo.findByStaOcorrenciaOrderByDtaOcorrenciaDesc(parseStatus(status), pageable);

        return ocorrencias.map(this::toResponse);
    }

    public Flux<OcorrenciaResponseDTO> streamTodas() {
        return repo.findAllCursor().map(this::toResponse);
    }

    private StatusOcorrencia parseStatus(String status) {
        try {
            return StatusOcorrencia.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status inválido: use ATIVA ou FINALIZADA");
        }
    }

    private OcorrenciaResponseDTO toResponse(Ocorrencia o) {
        return new OcorrenciaResponseDTO(o.codOcorrencia(), null, null, null, null,
                o.dtaOcorrencia(), o.staOcorrencia(), null);
    }
}
