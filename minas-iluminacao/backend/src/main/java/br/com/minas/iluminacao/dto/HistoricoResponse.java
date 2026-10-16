package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.HistoricoOperacao;
import java.time.LocalDateTime;

public record HistoricoResponse(Long id, LocalDateTime dataHora, Long torreId, String torreNome,
                                String operacao, String usuario) {
    public static HistoricoResponse de(HistoricoOperacao h) {
        return new HistoricoResponse(h.getId(), h.getDataHora(),
                h.getTorre() != null ? h.getTorre().getId() : null,
                h.getTorreNome(), h.getOperacao(),
                h.getUsuario() != null ? h.getUsuario().getNome() : "Sistema");
    }
}
