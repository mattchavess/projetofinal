package br.com.minas.iluminacao.evento;

/** Publicado antes de uma torre ser excluída, para outros módulos limparem seus dados. */
public record TorreExcluidaEvento(Long torreId) {}
