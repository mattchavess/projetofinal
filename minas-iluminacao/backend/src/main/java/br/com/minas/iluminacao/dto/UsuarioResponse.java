package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.Usuario;
import br.com.minas.iluminacao.model.enums.PerfilUsuario;

public record UsuarioResponse(Long id, String nome, String email, PerfilUsuario perfil, boolean ativo) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.isAtivo());
    }
}
