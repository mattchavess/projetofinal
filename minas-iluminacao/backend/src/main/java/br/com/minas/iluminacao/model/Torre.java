package br.com.minas.iluminacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import br.com.minas.iluminacao.model.enums.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "torre")
@Getter @Setter @NoArgsConstructor
public class Torre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(length = 200)
    private String localizacao;

    private Double latitude;
    private Double longitude;

    @ManyToOne(optional = false)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    @Enumerated(EnumType.STRING)
    @Column(name = "fonte_energia", nullable = false, length = 20)
    private FonteEnergia fonteEnergia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTorre status;

    /** Percentual de 0 a 100. */
    @Column(nullable = false)
    private Integer bateria = 100;

    /** Percentual de 0 a 100. Só se aplica a torres com gerador. */
    private Integer combustivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_iluminacao", nullable = false, length = 25)
    private ModoIluminacao modoIluminacao = ModoIluminacao.NORMAL;

    /** Potência nominal, usada na estimativa de consumo. */
    @Column(name = "consumo_watts", nullable = false)
    private Integer consumoWatts = 400;

    @Column(name = "horas_atividade", nullable = false)
    private Double horasAtividade = 0.0;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "ultima_atualizacao", nullable = false)
    private LocalDateTime ultimaAtualizacao;

    @Column(name = "ultima_comunicacao")
    private LocalDateTime ultimaComunicacao;

    @PrePersist
    void aoCriar() {
        dataCadastro = LocalDateTime.now();
        ultimaAtualizacao = dataCadastro;
    }

    @PreUpdate
    void aoAtualizar() { ultimaAtualizacao = LocalDateTime.now(); }
}
