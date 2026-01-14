package br.com.carbigdata.ocorrencias_reactive.domain.dto;

import br.com.carbigdata.ocorrencias_reactive.domain.model.Ocorrencia;
import br.com.carbigdata.ocorrencias_reactive.domain.model.StatusOcorrencia;
import lombok.Data;

@Data
public class OcorrenciaResponse {
    private Long codOcorrencia;
    private String nmeCliente;
    private String nmeLogradouro;
    private StatusOcorrencia status;

    public static OcorrenciaResponse from(Ocorrencia o, String nmeCliente, String nmeLogradouro) {
        OcorrenciaResponse res = new OcorrenciaResponse();
        res.codOcorrencia = o.codOcorrencia();
        res.nmeCliente = nmeCliente;
        res.nmeLogradouro = nmeLogradouro;
        res.status = o.staOcorrencia();
        return res;
    }
}