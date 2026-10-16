package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.Manutencao;
import br.com.minas.iluminacao.model.enums.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ManutencaoResponse(
        Long id, Long torreId, String torreNome, TipoManutencao tipo, String descricao,
        String responsavel, LocalDateTime dataAbertura, LocalDate dataPrevista,
        LocalDateTime dataConclusao, StatusManutencao status, String observacoes
) {
    public static ManutencaoResponse de(Manutencao m) {
        return new ManutencaoResponse(m.getId(), m.getTorre().getId(), m.getTorre().getNome(),
                m.getTipo(), m.getDescricao(), m.getResponsavel(), m.getDataAbertura(),
                m.getDataPrevista(), m.getDataConclusao(), m.getStatus(), m.getObservacoes());
    }
}
