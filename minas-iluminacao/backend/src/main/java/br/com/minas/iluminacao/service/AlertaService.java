package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.AlertaRequest;
import br.com.minas.iluminacao.dto.AlertaResponse;
import br.com.minas.iluminacao.exception.RecursoNaoEncontradoException;
import br.com.minas.iluminacao.exception.RegraNegocioException;
import br.com.minas.iluminacao.model.Alerta;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.*;
import br.com.minas.iluminacao.repository.AlertaRepository;
import br.com.minas.iluminacao.repository.TorreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final TorreRepository torreRepository;
    private final HistoricoService historicoService;

    @Value("${app.limites.bateria-critica}") private int bateriaCritica;
    @Value("${app.limites.bateria-atencao}") private int bateriaAtencao;
    @Value("${app.limites.combustivel-critico}") private int combustivelCritico;
    @Value("${app.limites.combustivel-atencao}") private int combustivelAtencao;

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar(StatusAlerta status, NivelAlerta nivel, Long torreId) {
        return alertaRepository.pesquisar(status, nivel, torreId).stream().map(AlertaResponse::de).toList();
    }

    /** Alerta criado manualmente pelo operador (ex.: falha técnica). */
    @Transactional
    public AlertaResponse criar(AlertaRequest req) {
        Torre torre = torreRepository.findById(req.torreId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Torre não encontrada"));
        Alerta alerta = registrarAlerta(torre, req.tipo(), req.nivel(), req.descricao());
        return AlertaResponse.de(alerta);
    }

    @Transactional
    public AlertaResponse resolver(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado"));
        if (alerta.getStatus() == StatusAlerta.RESOLVIDO) {
            throw new RegraNegocioException("Alerta já está resolvido");
        }
        alerta.setStatus(StatusAlerta.RESOLVIDO);
        alerta.setDataResolucao(LocalDateTime.now());
        historicoService.registrar(alerta.getTorre(), "Alerta resolvido: " + alerta.getDescricao());
        return AlertaResponse.de(alertaRepository.save(alerta));
    }

    /** Cria o alerta apenas se ainda não houver um ativo do mesmo tipo para a torre. */
    @Transactional
    public void alertarSeNaoExistir(Torre torre, TipoAlerta tipo, NivelAlerta nivel, String descricao) {
        if (alertaRepository.existsByTorreIdAndTipoAndStatus(torre.getId(), tipo, StatusAlerta.ATIVO)) return;
        registrarAlerta(torre, tipo, nivel, descricao);
    }

    /** Resolve automaticamente os alertas ativos de um tipo (ex.: comunicação restabelecida). */
    @Transactional
    public void resolverAtivos(Torre torre, TipoAlerta tipo, String motivo) {
        List<Alerta> ativos = alertaRepository.findByTorreIdAndTipoAndStatus(torre.getId(), tipo, StatusAlerta.ATIVO);
        for (Alerta a : ativos) {
            a.setStatus(StatusAlerta.RESOLVIDO);
            a.setDataResolucao(LocalDateTime.now());
            alertaRepository.save(a);
        }
        if (!ativos.isEmpty()) historicoService.registrar(torre, motivo);
    }

    /**
     * Gera alertas de bateria/combustível quando os níveis estão abaixo dos limites.
     * Não duplica: se já existe alerta ativo do mesmo tipo para a torre, nada é criado.
     */
    @Transactional
    public void verificarNiveis(Torre torre) {
        avaliar(torre, TipoAlerta.BATERIA_BAIXA, torre.getBateria(),
                bateriaCritica, bateriaAtencao, "Bateria crítica", "Bateria baixa");
        avaliar(torre, TipoAlerta.COMBUSTIVEL_BAIXO, torre.getCombustivel(),
                combustivelCritico, combustivelAtencao, "Combustível crítico", "Combustível baixo");
    }

    private void avaliar(Torre torre, TipoAlerta tipo, Integer valor, int critico, int atencao,
                         String textoCritico, String textoBaixo) {
        if (valor == null) return;
        NivelAlerta nivel;
        String texto;
        if (valor < critico) {
            nivel = NivelAlerta.CRITICO;
            texto = textoCritico;
        } else if (valor < atencao) {
            nivel = NivelAlerta.ATENCAO;
            texto = textoBaixo;
        } else {
            return;
        }
        if (alertaRepository.existsByTorreIdAndTipoAndStatus(torre.getId(), tipo, StatusAlerta.ATIVO)) return;
        String descricao = texto + " — " + torre.getNome() + " — " + torre.getSetor().getNome();
        registrarAlerta(torre, tipo, nivel, descricao);
    }

    private Alerta registrarAlerta(Torre torre, TipoAlerta tipo, NivelAlerta nivel, String descricao) {
        Alerta alerta = new Alerta();
        alerta.setTorre(torre);
        alerta.setTipo(tipo);
        alerta.setNivel(nivel);
        alerta.setDescricao(descricao);
        alerta = alertaRepository.save(alerta);
        historicoService.registrar(torre, "Alerta (" + nivel.name().toLowerCase() + "): " + descricao);
        return alerta;
    }
}
