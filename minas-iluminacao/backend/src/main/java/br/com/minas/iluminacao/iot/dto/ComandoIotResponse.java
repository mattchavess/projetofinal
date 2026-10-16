package br.com.minas.iluminacao.iot.dto;

import br.com.minas.iluminacao.model.enums.ComandoTorre;

public record ComandoIotResponse(Long id, ComandoTorre comando) {}
