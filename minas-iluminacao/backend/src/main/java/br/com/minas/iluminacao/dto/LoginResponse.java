package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.PerfilUsuario;

public record LoginResponse(String token, String nome, String email, PerfilUsuario perfil) {}
