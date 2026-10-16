package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.*;
import java.time.LocalDateTime;

public record TorreResponse(
        Long id, String nome, String codigo, String localizacao,
        Double latitude, Double longitude,
        Long setorId, String setor,
        FonteEnergia fonteEnergia, StatusTorre status,
        Integer bateria, Integer combustivel,
        ModoIluminacao modoIluminacao, Integer consumoWatts, Double horasAtividade,
        LocalDateTime dataCadastro, LocalDateTime ultimaAtualizacao, LocalDateTime ultimaComunicacao
) {
    public static TorreResponse de(Torre t) {
        return new TorreResponse(t.getId(), t.getNome(), t.getCodigo(), t.getLocalizacao(),
                t.getLatitude(), t.getLongitude(),
                t.getSetor().getId(), t.getSetor().getNome(),
                t.getFonteEnergia(), t.getStatus(),
                t.getBateria(), t.getCombustivel(),
                t.getModoIluminacao(), t.getConsumoWatts(), t.getHorasAtividade(),
                t.getDataCadastro(), t.getUltimaAtualizacao(), t.getUltimaComunicacao());
    }
}
