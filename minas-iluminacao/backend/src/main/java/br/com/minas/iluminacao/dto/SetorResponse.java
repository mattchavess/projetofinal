package br.com.minas.iluminacao.dto;

import br.com.minas.iluminacao.model.Setor;

public record SetorResponse(Long id, String nome) {
    public static SetorResponse de(Setor s) { return new SetorResponse(s.getId(), s.getNome()); }
}
