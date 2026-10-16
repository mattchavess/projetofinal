package br.com.minas.iluminacao.iot.dto;

import br.com.minas.iluminacao.model.enums.ModoIluminacao;
import br.com.minas.iluminacao.model.enums.StatusTorre;

public record TelemetriaResponse(Long torreId, StatusTorre status, ModoIluminacao modo, boolean comandoPendente) {}
