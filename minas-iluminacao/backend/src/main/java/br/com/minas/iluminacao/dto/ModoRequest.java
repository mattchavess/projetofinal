package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.ModoIluminacao;
import jakarta.validation.constraints.NotNull;

public record ModoRequest(@NotNull(message = "Modo é obrigatório") ModoIluminacao modo) {}
