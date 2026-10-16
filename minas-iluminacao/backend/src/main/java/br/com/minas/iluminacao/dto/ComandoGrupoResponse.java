package br.com.minas.iluminacao.dto;

import java.util.List;

/** aplicadas = torres alteradas; ignoradas = mensagens sobre torres que não puderam receber o comando. */
public record ComandoGrupoResponse(int aplicadas, List<String> ignoradas) {}
