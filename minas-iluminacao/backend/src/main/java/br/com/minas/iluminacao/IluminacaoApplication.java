package br.com.minas.iluminacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IluminacaoApplication {
    public static void main(String[] args) {
        SpringApplication.run(IluminacaoApplication.class, args);
    }
}
