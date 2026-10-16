package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.ComandoTorre;
import jakarta.validation.constraints.NotNull;

public record ComandoRequest(@NotNull(message = "Comando é obrigatório") ComandoTorre comando) {}
