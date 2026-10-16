package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.*;
import jakarta.validation.constraints.*;

public record TorreRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 100) String nome,
        @NotBlank(message = "Código é obrigatório") @Size(max = 30) String codigo,
        @Size(max = 200) String localizacao,
        Double latitude,
        Double longitude,
        @NotNull(message = "Setor é obrigatório") Long setorId,
        @NotNull(message = "Fonte de energia é obrigatória") FonteEnergia fonteEnergia,
        @NotNull(message = "Status é obrigatório") StatusTorre status,
        @NotNull(message = "Bateria é obrigatória") @Min(0) @Max(100) Integer bateria,
        @Min(0) @Max(100) Integer combustivel,
        @NotNull(message = "Modo de iluminação é obrigatório") ModoIluminacao modoIluminacao,
        @NotNull(message = "Consumo é obrigatório") @Min(1) Integer consumoWatts
) {}
