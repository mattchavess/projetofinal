package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.enums.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Usada para criar e editar.
 * iniciar (só na criação): abre já "Em andamento" e coloca a torre em manutenção.
 * devolverTorre (ao concluir): devolve a torre para operação (Ligada).
 */
public record ManutencaoRequest(
        @NotNull(message = "Torre é obrigatória") Long torreId,
        @NotNull(message = "Tipo é obrigatório") TipoManutencao tipo,
        @NotBlank(message = "Descrição é obrigatória") @Size(max = 500) String descricao,
        @Size(max = 100) String responsavel,
        LocalDate dataPrevista,
        @Size(max = 500) String observacoes,
        StatusManutencao status,
        Boolean iniciar,
        Boolean devolverTorre
) {}
