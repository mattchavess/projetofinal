package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.*;
import br.com.minas.iluminacao.exception.RecursoNaoEncontradoException;
import br.com.minas.iluminacao.exception.RegraNegocioException;
import br.com.minas.iluminacao.model.Setor;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.*;
import br.com.minas.iluminacao.repository.*;
import br.com.minas.iluminacao.evento.ComandoAplicadoEvento;
import br.com.minas.iluminacao.evento.TorreExcluidaEvento;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TorreService {

    private final TorreRepository torreRepository;
    private final SetorRepository setorRepository;
    private final AlertaRepository alertaRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final LeituraSensorRepository leituraRepository;
    private final HistoricoOperacaoRepository historicoRepository;
    private final AlertaService alertaService;
    private final HistoricoService historicoService;
    private final ApplicationEventPublisher eventos;

    // ---------- consultas ----------

    @Transactional(readOnly = true)
    public List<TorreResponse> listar(String busca, StatusTorre status, Long setorId) {
        String termo = busca == null ? "" : busca.trim();
        return torreRepository.pesquisar(termo, status, setorId).stream().map(TorreResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TorreResponse buscar(Long id) {
        return TorreResponse.de(buscarEntidade(id));
    }

    public Torre buscarEntidade(Long id) {
        return torreRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Torre não encontrada"));
    }

    // ---------- cadastro ----------

    @Transactional
    public TorreResponse criar(TorreRequest req) {
        if (torreRepository.existsByCodigo(req.codigo())) {
            throw new RegraNegocioException("Já existe uma torre com o código " + req.codigo());
        }
        Torre torre = new Torre();
        preencher(torre, req);
        torre = torreRepository.save(torre);
        historicoService.registrar(torre, "Torre " + torre.getNome() + " cadastrada.");
        alertaService.verificarNiveis(torre);
        return TorreResponse.de(torre);
    }

    @Transactional
    public TorreResponse atualizar(Long id, TorreRequest req) {
        Torre torre = buscarEntidade(id);
        if (torreRepository.existsByCodigoAndIdNot(req.codigo(), id)) {
            throw new RegraNegocioException("Já existe outra torre com o código " + req.codigo());
        }
        preencher(torre, req);
        torre = torreRepository.save(torre);
        historicoService.registrar(torre, "Torre " + torre.getNome() + " atualizada.");
        alertaService.verificarNiveis(torre);
        return TorreResponse.de(torre);
    }

    @Transactional
    public void excluir(Long id) {
        Torre torre = buscarEntidade(id);
        String nome = torre.getNome();
        eventos.publishEvent(new TorreExcluidaEvento(id));
        alertaRepository.deleteByTorreId(id);
        manutencaoRepository.deleteByTorreId(id);
        leituraRepository.deleteByTorreId(id);
        historicoRepository.desvincularTorre(id);
        torreRepository.delete(torre);
        historicoService.registrarSemTorre(nome, "Torre " + nome + " excluída.");
    }

    // ---------- controle ----------

    @Transactional
    public TorreResponse alterarStatus(Long id, StatusTorre novoStatus) {
        Torre torre = buscarEntidade(id);
        exigirForaDeManutencao(torre);
        torre.setStatus(novoStatus);
        torreRepository.save(torre);
        historicoService.registrar(torre, "Torre " + torre.getNome() + " teve o status alterado para "
                + novoStatus.name().toLowerCase().replace('_', ' ') + ".");
        return TorreResponse.de(torre);
    }

    @Transactional
    public TorreResponse alterarModo(Long id, ModoIluminacao modo) {
        Torre torre = buscarEntidade(id);
        exigirForaDeManutencao(torre);
        aplicarComando(torre, ComandoTorre.deModo(modo));
        return TorreResponse.de(torre);
    }

    @Transactional
    public TorreResponse executarComando(Long id, ComandoTorre comando) {
        Torre torre = buscarEntidade(id);
        exigirForaDeManutencao(torre);
        aplicarComando(torre, comando);
        return TorreResponse.de(torre);
    }

    @Transactional
    public ComandoGrupoResponse executarComandoGrupo(ComandoGrupoRequest req) {
        int aplicadas = 0;
        List<String> ignoradas = new ArrayList<>();
        for (Long id : req.torreIds().stream().distinct().toList()) {
            Torre torre = torreRepository.findById(id).orElse(null);
            if (torre == null) {
                ignoradas.add("Torre " + id + " não encontrada");
            } else if (torre.getStatus() == StatusTorre.EM_MANUTENCAO) {
                ignoradas.add(torre.getNome() + " está em manutenção");
            } else {
                aplicarComando(torre, req.comando());
                aplicadas++;
            }
        }
        return new ComandoGrupoResponse(aplicadas, ignoradas);
    }

    // ---------- auxiliares ----------

    private void aplicarComando(Torre torre, ComandoTorre comando) {
        switch (comando) {
            case LIGAR -> torre.setStatus(StatusTorre.LIGADA);
            case DESLIGAR -> torre.setStatus(StatusTorre.DESLIGADA);
            case ECONOMICO -> torre.setModoIluminacao(ModoIluminacao.ECONOMICO);
            case NORMAL -> torre.setModoIluminacao(ModoIluminacao.NORMAL);
            case INTENSIDADE_MAXIMA -> torre.setModoIluminacao(ModoIluminacao.INTENSIDADE_MAXIMA);
            case CERRACAO -> torre.setModoIluminacao(ModoIluminacao.CERRACAO);
        }
        torreRepository.save(torre);
        historicoService.registrar(torre, "Torre " + torre.getNome() + " " + comando.getDescricao() + ".");
        eventos.publishEvent(new ComandoAplicadoEvento(torre, comando));
    }

    private void exigirForaDeManutencao(Torre torre) {
        if (torre.getStatus() == StatusTorre.EM_MANUTENCAO) {
            throw new RegraNegocioException(
                    "A torre está em manutenção. Conclua a manutenção para voltar a controlá-la.");
        }
    }

    private void preencher(Torre torre, TorreRequest req) {
        Setor setor = setorRepository.findById(req.setorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));
        torre.setNome(req.nome().trim());
        torre.setCodigo(req.codigo().trim());
        torre.setLocalizacao(req.localizacao());
        torre.setLatitude(req.latitude());
        torre.setLongitude(req.longitude());
        torre.setSetor(setor);
        torre.setFonteEnergia(req.fonteEnergia());
        torre.setStatus(req.status());
        torre.setBateria(req.bateria());
        // combustível só faz sentido para torres a gerador
        torre.setCombustivel(req.fonteEnergia() == FonteEnergia.GERADOR ? req.combustivel() : null);
        torre.setModoIluminacao(req.modoIluminacao());
        torre.setConsumoWatts(req.consumoWatts());
    }
}
