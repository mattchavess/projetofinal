package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.*;
import jakarta.validation.constraints.*;

public record AlertaRequest(
        @NotNull(message = "Torre é obrigatória") Long torreId,
        @NotNull(message = "Tipo é obrigatório") TipoAlerta tipo,
        @NotNull(message = "Nível é obrigatório") NivelAlerta nivel,
        @NotBlank(message = "Descrição é obrigatória") @Size(max = 255) String descricao
) {}
