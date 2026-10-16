package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.UsuarioRequest;
import br.com.minas.iluminacao.dto.UsuarioResponse;
import br.com.minas.iluminacao.exception.RecursoNaoEncontradoException;
import br.com.minas.iluminacao.exception.RegraNegocioException;
import br.com.minas.iluminacao.model.Usuario;
import br.com.minas.iluminacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorEmail(String email) {
        return repository.findByEmail(email).map(UsuarioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest req) {
        String email = req.email().trim().toLowerCase();
        if (repository.findByEmail(email).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário com este e-mail");
        }
        if (req.senha() == null || req.senha().isBlank()) {
            throw new RegraNegocioException("Senha é obrigatória para novo usuário");
        }
        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setSenhaHash(passwordEncoder.encode(req.senha()));
        u.setPerfil(req.perfil());
        u.setAtivo(req.ativo() == null || req.ativo());
        return UsuarioResponse.de(repository.save(u));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest req, String emailLogado) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        String email = req.email().trim().toLowerCase();
        repository.findByEmail(email).ifPresent(outro -> {
            if (!outro.getId().equals(id)) throw new RegraNegocioException("Já existe um usuário com este e-mail");
        });
        boolean ativo = req.ativo() == null || req.ativo();
        if (u.getEmail().equals(emailLogado) && (!ativo || req.perfil() != u.getPerfil())) {
            throw new RegraNegocioException("Você não pode desativar nem alterar o perfil da sua própria conta");
        }
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setPerfil(req.perfil());
        u.setAtivo(ativo);
        if (req.senha() != null && !req.senha().isBlank()) {
            u.setSenhaHash(passwordEncoder.encode(req.senha()));
        }
        return UsuarioResponse.de(repository.save(u));
    }
}
