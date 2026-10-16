package br.com.minas.iluminacao.iot.model;

import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.ComandoTorre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Fila de comandos que o dispositivo da torre ainda precisa buscar (polling). */
@Entity
@Table(name = "comando_pendente")
@Getter @Setter @NoArgsConstructor
public class ComandoPendente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "torre_id")
    private Torre torre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ComandoTorre comando;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(nullable = false)
    private boolean entregue = false;

    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;
}
