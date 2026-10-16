package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.HistoricoResponse;
import br.com.minas.iluminacao.model.HistoricoOperacao;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.Usuario;
import br.com.minas.iluminacao.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.minas.iluminacao.repository.HistoricoOperacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final HistoricoOperacaoRepository repository;
    private final UsuarioRepository usuarioRepository;

    /** Registra uma operação ligada a uma torre, com o usuário logado (ou "Sistema" se não houver). */
    @Transactional
    public void registrar(Torre torre, String operacao) {
        HistoricoOperacao h = new HistoricoOperacao();
        h.setTorre(torre);
        h.setTorreNome(torre.getNome());
        h.setOperacao(operacao);
        h.setUsuario(usuarioAtual());
        repository.save(h);
    }

    /** Usado quando a torre já não existe mais (exclusão). */
    @Transactional
    public void registrarSemTorre(String torreNome, String operacao) {
        HistoricoOperacao h = new HistoricoOperacao();
        h.setTorreNome(torreNome);
        h.setOperacao(operacao);
        h.setUsuario(usuarioAtual());
        repository.save(h);
    }

    private Usuario usuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof String email)) return null;
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> listar(Long torreId) {
        List<HistoricoOperacao> lista = torreId == null
                ? repository.findTop200ByOrderByDataHoraDesc()
                : repository.findTop200ByTorreIdOrderByDataHoraDesc(torreId);
        return lista.stream().map(HistoricoResponse::de).toList();
    }
}
