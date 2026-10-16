package br.com.minas.iluminacao.iot.service;

import br.com.minas.iluminacao.exception.RecursoNaoEncontradoException;
import br.com.minas.iluminacao.iot.dto.TelemetriaRequest;
import br.com.minas.iluminacao.iot.dto.TelemetriaResponse;
import br.com.minas.iluminacao.model.LeituraSensor;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.StatusTorre;
import br.com.minas.iluminacao.model.enums.TipoAlerta;
import br.com.minas.iluminacao.repository.LeituraSensorRepository;
import br.com.minas.iluminacao.repository.TorreRepository;
import br.com.minas.iluminacao.service.AlertaService;
import br.com.minas.iluminacao.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TelemetriaService {

    private static final double MAX_HORAS_POR_LEITURA = 5.0 / 60; // evita somar longos períodos sem sinal

    private final TorreRepository torreRepository;
    private final LeituraSensorRepository leituraRepository;
    private final ComandoPendenteService comandoService;
    private final AlertaService alertaService;
    private final HistoricoService historicoService;

    @Transactional
    public TelemetriaResponse registrar(TelemetriaRequest req) {
        Torre torre = torreRepository.findById(req.torreId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Torre não encontrada"));
        LocalDateTime agora = LocalDateTime.now();

        // tempo de atividade: soma o intervalo desde a última comunicação se a torre estava ligada
        if (torre.getStatus() == StatusTorre.LIGADA && torre.getUltimaComunicacao() != null) {
            double horas = Duration.between(torre.getUltimaComunicacao(), agora).toMillis() / 3_600_000.0;
            torre.setHorasAtividade(torre.getHorasAtividade() + Math.min(horas, MAX_HORAS_POR_LEITURA));
        }

        LeituraSensor leitura = new LeituraSensor();
        leitura.setTorre(torre);
        leitura.setLuminosidade(req.luminosidade());
        leitura.setBateria(req.bateria());
        leitura.setDataHora(agora);
        leituraRepository.save(leitura);

        torre.setUltimaComunicacao(agora);
        if (req.bateria() != null) torre.setBateria(req.bateria());

        // O estado informado pelo dispositivo só vale se não houver comando do operador a caminho
        // (senão a telemetria antiga desfaria o comando antes de o dispositivo buscá-lo).
        boolean pendente = comandoService.temPendente(torre.getId());
        StatusTorre informado = req.status();
        boolean aceitavel = informado == StatusTorre.LIGADA || informado == StatusTorre.DESLIGADA;
        if (!pendente && aceitavel && torre.getStatus() != StatusTorre.EM_MANUTENCAO && informado != torre.getStatus()) {
            torre.setStatus(informado);
            historicoService.registrar(torre, "Torre " + torre.getNome() + " informou estado "
                    + informado.name().toLowerCase() + " (dispositivo).");
        }
        torreRepository.save(torre);

        alertaService.verificarNiveis(torre);
        alertaService.resolverAtivos(torre, TipoAlerta.SEM_COMUNICACAO,
                "Comunicação com " + torre.getNome() + " restabelecida.");

        return new TelemetriaResponse(torre.getId(), torre.getStatus(), torre.getModoIluminacao(), pendente);
    }
}
