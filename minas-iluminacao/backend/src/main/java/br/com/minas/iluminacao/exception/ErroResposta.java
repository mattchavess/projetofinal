package br.com.minas.iluminacao.exception;

import java.time.LocalDateTime;

public record ErroResposta(int status, String erro, String mensagem, LocalDateTime timestamp) {
    public static ErroResposta de(int status, String erro, String mensagem) {
        return new ErroResposta(status, erro, mensagem, LocalDateTime.now());
    }
}
