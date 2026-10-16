package br.com.minas.iluminacao.iot.dto;

import br.com.minas.iluminacao.model.enums.StatusTorre;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TelemetriaRequest(
        @NotNull(message = "torreId é obrigatório") Long torreId,
        @Min(0) Integer luminosidade,
        @Min(0) @Max(100) Integer bateria,
        StatusTorre status
) {}
