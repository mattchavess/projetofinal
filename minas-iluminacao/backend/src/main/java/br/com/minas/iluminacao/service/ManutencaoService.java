package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.ManutencaoRequest;
import br.com.minas.iluminacao.dto.ManutencaoResponse;
import br.com.minas.iluminacao.exception.RecursoNaoEncontradoException;
import br.com.minas.iluminacao.exception.RegraNegocioException;
import br.com.minas.iluminacao.model.Manutencao;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.StatusManutencao;
import br.com.minas.iluminacao.model.enums.StatusTorre;
import br.com.minas.iluminacao.repository.ManutencaoRepository;
import br.com.minas.iluminacao.repository.TorreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final TorreRepository torreRepository;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public List<ManutencaoResponse> listar(StatusManutencao status, Long torreId) {
        return manutencaoRepository.pesquisar(status, torreId).stream().map(ManutencaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ManutencaoResponse buscar(Long id) {
        return ManutencaoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ManutencaoResponse criar(ManutencaoRequest req) {
        Torre torre = torreRepository.findById(req.torreId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Torre não encontrada"));

        Manutencao m = new Manutencao();
        m.setTorre(torre);
        copiarCampos(m, req);
        m.setStatus(StatusManutencao.ABERTA);
        m = manutencaoRepository.save(m);
        historicoService.registrar(torre, "Manutenção " + m.getTipo().name().toLowerCase()
                + " aberta para " + torre.getNome() + ".");

        if (Boolean.TRUE.equals(req.iniciar())) {
            iniciar(m);
        }
        return ManutencaoResponse.de(m);
    }

    @Transactional
    public ManutencaoResponse atualizar(Long id, ManutencaoRequest req) {
        Manutencao m = buscarEntidade(id);
        if (m.getStatus() == StatusManutencao.CONCLUIDA || m.getStatus() == StatusManutencao.CANCELADA) {
            throw new RegraNegocioException("Manutenção " + m.getStatus().name().toLowerCase()
                    + " não pode mais ser alterada");
        }
        copiarCampos(m, req);

        StatusManutencao novo = req.status() != null ? req.status() : m.getStatus();
        switch (novo) {
            case EM_ANDAMENTO -> iniciar(m);
            case CONCLUIDA -> finalizar(m, StatusManutencao.CONCLUIDA, Boolean.TRUE.equals(req.devolverTorre()));
            case CANCELADA -> finalizar(m, StatusManutencao.CANCELADA, Boolean.TRUE.equals(req.devolverTorre()));
            case ABERTA -> m.setStatus(StatusManutencao.ABERTA);
        }
        return ManutencaoResponse.de(manutencaoRepository.save(m));
    }

    /** Coloca a manutenção em andamento e a torre em "Em manutenção". */
    private void iniciar(Manutencao m) {
        m.setStatus(StatusManutencao.EM_ANDAMENTO);
        Torre torre = m.getTorre();
        if (torre.getStatus() != StatusTorre.EM_MANUTENCAO) {
            torre.setStatus(StatusTorre.EM_MANUTENCAO);
            torreRepository.save(torre);
            historicoService.registrar(torre, "Torre " + torre.getNome() + " entrou em manutenção.");
        }
        manutencaoRepository.save(m);
    }

    private void finalizar(Manutencao m, StatusManutencao status, boolean devolverTorre) {
        m.setStatus(status);
        if (status == StatusManutencao.CONCLUIDA) m.setDataConclusao(LocalDateTime.now());
        Torre torre = m.getTorre();
        historicoService.registrar(torre, "Manutenção " + status.name().toLowerCase()
                + " para " + torre.getNome() + ".");
        if (devolverTorre && torre.getStatus() == StatusTorre.EM_MANUTENCAO) {
            torre.setStatus(StatusTorre.LIGADA);
            torreRepository.save(torre);
            historicoService.registrar(torre, "Torre " + torre.getNome() + " voltou à operação.");
        }
    }

    private void copiarCampos(Manutencao m, ManutencaoRequest req) {
        m.setTipo(req.tipo());
        m.setDescricao(req.descricao());
        m.setResponsavel(req.responsavel());
        m.setDataPrevista(req.dataPrevista());
        m.setObservacoes(req.observacoes());
    }

    private Manutencao buscarEntidade(Long id) {
        return manutencaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Manutenção não encontrada"));
    }
}
