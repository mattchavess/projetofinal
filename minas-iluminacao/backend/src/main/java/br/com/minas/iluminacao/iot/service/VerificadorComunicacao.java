package br.com.minas.iluminacao.iot.service;

import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.NivelAlerta;
import br.com.minas.iluminacao.model.enums.TipoAlerta;
import br.com.minas.iluminacao.repository.LeituraSensorRepository;
import br.com.minas.iluminacao.repository.TorreRepository;
import br.com.minas.iluminacao.service.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Rotina periódica: alerta de ausência de comunicação e limpeza de leituras antigas. */
@Component
@RequiredArgsConstructor
public class VerificadorComunicacao {

    private final TorreRepository torreRepository;
    private final LeituraSensorRepository leituraRepository;
    private final AlertaService alertaService;

    @Value("${app.iot.timeout-minutos}")
    private long timeoutMinutos;

    @Value("${app.iot.retencao-leituras-dias}")
    private long retencaoDias;

    @Scheduled(fixedDelayString = "${app.iot.verificacao-ms}", initialDelayString = "${app.iot.verificacao-ms}")
    @Transactional
    public void verificar() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(timeoutMinutos);
        for (Torre t : torreRepository.buscarSemComunicacao(limite)) {
            alertaService.alertarSeNaoExistir(t, TipoAlerta.SEM_COMUNICACAO, NivelAlerta.ATENCAO,
                    "Sem comunicação há mais de " + timeoutMinutos + " min — " + t.getNome() + " — " + t.getSetor().getNome());
        }
        leituraRepository.apagarAnterioresA(LocalDateTime.now().minusDays(retencaoDias));
    }
}
