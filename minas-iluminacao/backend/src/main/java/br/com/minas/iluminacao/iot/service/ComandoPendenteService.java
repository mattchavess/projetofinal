package br.com.minas.iluminacao.iot.service;

import br.com.minas.iluminacao.evento.ComandoAplicadoEvento;
import br.com.minas.iluminacao.evento.TorreExcluidaEvento;
import br.com.minas.iluminacao.exception.RecursoNaoEncontradoException;
import br.com.minas.iluminacao.iot.dto.ComandoIotResponse;
import br.com.minas.iluminacao.iot.model.ComandoPendente;
import br.com.minas.iluminacao.iot.repository.ComandoPendenteRepository;
import br.com.minas.iluminacao.repository.LeituraSensorRepository;
import br.com.minas.iluminacao.repository.TorreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComandoPendenteService {

    private final ComandoPendenteRepository repository;
    private final LeituraSensorRepository leituraRepository;
    private final TorreRepository torreRepository;

    /**
     * Ouve os comandos aplicados pelo operador. Só enfileira para torres que já enviaram
     * telemetria (ou seja, que têm um dispositivo conectado); as demais seguem sem fila.
     */
    @EventListener
    @Transactional
    public void aoAplicarComando(ComandoAplicadoEvento evento) {
        if (!leituraRepository.existsByTorreId(evento.torre().getId())) return;
        ComandoPendente c = new ComandoPendente();
        c.setTorre(evento.torre());
        c.setComando(evento.comando());
        repository.save(c);
    }

    @EventListener
    @Transactional
    public void aoExcluirTorre(TorreExcluidaEvento evento) {
        repository.deleteByTorreId(evento.torreId());
    }

    /** Entrega o comando mais antigo ainda não entregue e o marca como entregue. */
    @Transactional
    public Optional<ComandoIotResponse> proximoComando(Long torreId) {
        if (!torreRepository.existsById(torreId)) {
            throw new RecursoNaoEncontradoException("Torre não encontrada");
        }
        return repository.findFirstByTorreIdAndEntregueFalseOrderByCriadoEmAsc(torreId).map(c -> {
            c.setEntregue(true);
            c.setEntregueEm(LocalDateTime.now());
            return new ComandoIotResponse(c.getId(), c.getComando());
        });
    }

    @Transactional(readOnly = true)
    public boolean temPendente(Long torreId) {
        return repository.existsByTorreIdAndEntregueFalse(torreId);
    }
}
