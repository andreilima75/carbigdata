package br.com.carbigdata.ocorrencias_reactive.adapter.web.controller;


import br.com.carbigdata.ocorrencias_reactive.adapter.web.dto.OcorrenciaRequestDTO;
import br.com.carbigdata.ocorrencias_reactive.adapter.web.dto.OcorrenciaResponseDTO;
import br.com.carbigdata.ocorrencias_reactive.adapter.web.service.IngestaoOcorrenciaService;
import br.com.carbigdata.ocorrencias_reactive.adapter.web.service.QueryOcorrenciaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {

    private final IngestaoOcorrenciaService ingestaoService;
    private final QueryOcorrenciaService queryService;

    public OcorrenciaController(IngestaoOcorrenciaService ingestaoService,
                                QueryOcorrenciaService queryService) {
        this.ingestaoService = ingestaoService;
        this.queryService = queryService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Void>> ingestao(
            @Valid @RequestPart("dados") OcorrenciaRequestDTO dto,
            @RequestPart("foto") Mono<FilePart> fotoPartMono) {

        return ingestaoService.publicarOcorrenciaComMono(dto, fotoPartMono)
                .then(Mono.fromCallable(() -> ResponseEntity.accepted().build()));
    }

    @GetMapping
    public Flux<OcorrenciaResponseDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return queryService.listarPaginado(page, size, status);
    }

    @GetMapping(value = "/stream", produces = "application/x-ndjson")
    public Flux<OcorrenciaResponseDTO> stream() {
        return queryService.streamTodas();
    }
}