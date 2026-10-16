package br.com.minas.iluminacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import br.com.minas.iluminacao.model.enums.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_operacao")
@Getter @Setter @NoArgsConstructor
public class HistoricoOperacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Pode ficar nulo se a torre for excluída; o nome fica guardado em torreNome. */
    @ManyToOne
    @JoinColumn(name = "torre_id")
    private Torre torre;

    @Column(name = "torre_nome", length = 100)
    private String torreNome;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 255)
    private String operacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();
}
