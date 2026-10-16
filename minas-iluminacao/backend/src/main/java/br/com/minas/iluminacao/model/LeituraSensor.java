package br.com.minas.iluminacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import br.com.minas.iluminacao.model.enums.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leitura_sensor")
@Getter @Setter @NoArgsConstructor
public class LeituraSensor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "torre_id")
    private Torre torre;

    private Integer luminosidade;
    private Integer bateria;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();
}
