package br.com.minas.iluminacao.model.enums;

public enum ModoIluminacao {
    ECONOMICO(0.5), NORMAL(1.0), INTENSIDADE_MAXIMA(1.3), CERRACAO(1.15);

    /** Fator usado na estimativa de consumo (1.0 = potência nominal). */
    private final double fatorConsumo;

    ModoIluminacao(double fatorConsumo) { this.fatorConsumo = fatorConsumo; }

    public double getFatorConsumo() { return fatorConsumo; }
}
