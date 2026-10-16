package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.PerfilUsuario;
import jakarta.validation.constraints.*;

/** Na edição, senha em branco mantém a senha atual. */
public record UsuarioRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 100) String nome,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @Size(min = 6, max = 72, message = "Senha deve ter de 6 a 72 caracteres") String senha,
        @NotNull(message = "Perfil é obrigatório") PerfilUsuario perfil,
        Boolean ativo) {}
