package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.ComandoTorre;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ComandoGrupoRequest(
        @NotEmpty(message = "Selecione ao menos uma torre") List<Long> torreIds,
        @NotNull(message = "Comando é obrigatório") ComandoTorre comando
) {}
