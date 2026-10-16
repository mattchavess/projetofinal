package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.LoginRequest;
import br.com.minas.iluminacao.dto.LoginResponse;
import br.com.minas.iluminacao.exception.AutenticacaoException;
import br.com.minas.iluminacao.model.Usuario;
import br.com.minas.iluminacao.repository.UsuarioRepository;
import br.com.minas.iluminacao.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest req) {
        Usuario u = usuarioRepository.findByEmail(req.email().trim().toLowerCase())
                .filter(Usuario::isAtivo)
                .filter(x -> passwordEncoder.matches(req.senha(), x.getSenhaHash()))
                .orElseThrow(() -> new AutenticacaoException("E-mail ou senha inválidos"));
        return new LoginResponse(jwtService.gerar(u), u.getNome(), u.getEmail(), u.getPerfil());
    }
}
