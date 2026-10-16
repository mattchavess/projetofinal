package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.StatusTorre;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "Status é obrigatório") StatusTorre status) {}
