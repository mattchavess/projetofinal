package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.Alerta;
import br.com.minas.iluminacao.model.enums.*;
import java.time.LocalDateTime;

public record AlertaResponse(
        Long id, Long torreId, String torreNome, String setor,
        TipoAlerta tipo, NivelAlerta nivel, String descricao,
        LocalDateTime dataHora, StatusAlerta status, LocalDateTime dataResolucao
) {
    public static AlertaResponse de(Alerta a) {
        return new AlertaResponse(a.getId(), a.getTorre().getId(), a.getTorre().getNome(),
                a.getTorre().getSetor().getNome(), a.getTipo(), a.getNivel(), a.getDescricao(),
                a.getDataHora(), a.getStatus(), a.getDataResolucao());
    }
}
