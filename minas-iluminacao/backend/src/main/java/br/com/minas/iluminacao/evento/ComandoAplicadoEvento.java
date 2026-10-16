package br.com.minas.iluminacao.evento;

import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.ComandoTorre;

/** Publicado sempre que um comando (ligar, desligar, modo) é aplicado a uma torre. */
public record ComandoAplicadoEvento(Torre torre, ComandoTorre comando) {}
