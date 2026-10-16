package br.com.minas.iluminacao.model.enums;

public enum ComandoTorre {
    LIGAR("ligada"),
    DESLIGAR("desligada"),
    ECONOMICO("entrou em modo econômico"),
    NORMAL("entrou em modo normal"),
    INTENSIDADE_MAXIMA("entrou em modo intensidade máxima"),
    CERRACAO("entrou em modo cerração");

    private final String descricao;

    ComandoTorre(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }

    /** Os nomes dos modos são iguais aos dos comandos correspondentes. */
    public static ComandoTorre deModo(ModoIluminacao modo) { return valueOf(modo.name()); }
}
