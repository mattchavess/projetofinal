package br.com.minas.iluminacao.controller;

import br.com.minas.iluminacao.dto.LoginRequest;
import br.com.minas.iluminacao.dto.LoginResponse;
import br.com.minas.iluminacao.dto.UsuarioResponse;
import br.com.minas.iluminacao.service.AuthService;
import br.com.minas.iluminacao.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UsuarioResponse me(Authentication auth) {
        return usuarioService.buscarPorEmail((String) auth.getPrincipal());
    }
}
