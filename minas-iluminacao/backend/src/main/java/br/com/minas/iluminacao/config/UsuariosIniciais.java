package br.com.minas.iluminacao.config;

import br.com.minas.iluminacao.model.Usuario;
import br.com.minas.iluminacao.model.enums.PerfilUsuario;
import br.com.minas.iluminacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Cria o administrador e um operador de demonstração se ainda não existir nenhum usuário. */
@Component
@RequiredArgsConstructor
public class UsuariosIniciais implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    @Value("${app.admin.senha-inicial}")
    private String senhaInicial;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        criar("Administrador", "admin@minas.com", senhaInicial, PerfilUsuario.ADMIN);
        criar("Operador Demo", "operador@minas.com", "operador123", PerfilUsuario.OPERADOR);
    }

    private void criar(String nome, String email, String senha, PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(senha));
        u.setPerfil(perfil);
        repository.save(u);
    }
}
