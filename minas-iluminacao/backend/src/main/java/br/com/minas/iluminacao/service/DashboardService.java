package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.DashboardResponse;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.*;
import br.com.minas.iluminacao.repository.AlertaRepository;
import br.com.minas.iluminacao.repository.TorreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TorreRepository torreRepository;
    private final AlertaRepository alertaRepository;

    @Value("${app.limites.bateria-critica}")
    private int bateriaCritica;

    @Transactional(readOnly = true)
    public DashboardResponse montar() {
        List<Torre> torres = torreRepository.findAll();
        long total = torres.size();

        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (StatusTorre s : StatusTorre.values()) porStatus.put(s.name(), torreRepository.countByStatus(s));

        Map<String, Long> alertasNivel = new LinkedHashMap<>();
        for (NivelAlerta n : NivelAlerta.values())
            alertasNivel.put(n.name(), alertaRepository.countByStatusAndNivel(StatusAlerta.ATIVO, n));

        Map<String, Long> alertasTipo = new LinkedHashMap<>();
        for (TipoAlerta t : TipoAlerta.values())
            alertasTipo.put(t.name(), alertaRepository.countByStatusAndTipo(StatusAlerta.ATIVO, t));

        // consumo estimado (kW): só torres ligadas, potência nominal x fator do modo
        Map<String, Double> consumoSetor = new LinkedHashMap<>();
        double consumoTotal = 0;
        double horas = 0;
        for (Torre t : torres) {
            horas += t.getHorasAtividade();
            double kw = 0;
            if (t.getStatus() == StatusTorre.LIGADA || t.getStatus() == StatusTorre.ALERTA) {
                kw = t.getConsumoWatts() * t.getModoIluminacao().getFatorConsumo() / 1000.0;
            }
            consumoTotal += kw;
            consumoSetor.merge(t.getSetor().getNome(), kw, Double::sum);
        }
        consumoSetor.replaceAll((k, v) -> arredondar(v));

        // disponibilidade: torres que não estão em manutenção nem indisponíveis
        long indisponiveis = porStatus.get(StatusTorre.EM_MANUTENCAO.name())
                + porStatus.get(StatusTorre.INDISPONIVEL.name());
        double disponibilidade = total == 0 ? 0 : (total - indisponiveis) * 100.0 / total;

        return new DashboardResponse(
                total,
                porStatus.get(StatusTorre.LIGADA.name()),
                porStatus.get(StatusTorre.DESLIGADA.name()),
                porStatus.get(StatusTorre.EM_MANUTENCAO.name()),
                alertaRepository.contarTorresComAlerta(StatusAlerta.ATIVO),
                torreRepository.countByBateriaLessThan(bateriaCritica),
                arredondar(consumoTotal),
                arredondar(horas),
                arredondar(disponibilidade),
                porStatus, alertasNivel, alertasTipo, consumoSetor);
    }

    private double arredondar(double v) { return Math.round(v * 100.0) / 100.0; }
}
