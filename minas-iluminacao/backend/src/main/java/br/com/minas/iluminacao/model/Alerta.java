package br.com.minas.iluminacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import br.com.minas.iluminacao.model.enums.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
@Getter @Setter @NoArgsConstructor
public class Alerta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "torre_id")
    private Torre torre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelAlerta nivel;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAlerta status = StatusAlerta.ATIVO;

    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;
}
