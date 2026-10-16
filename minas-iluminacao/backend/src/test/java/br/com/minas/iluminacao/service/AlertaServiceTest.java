package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.model.Alerta;
import br.com.minas.iluminacao.model.Setor;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.*;
import br.com.minas.iluminacao.repository.AlertaRepository;
import br.com.minas.iluminacao.repository.TorreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock AlertaRepository alertaRepository;
    @Mock TorreRepository torreRepository;
    @Mock HistoricoService historicoService;
    @InjectMocks AlertaService service;

    @BeforeEach
    void limites() {
        ReflectionTestUtils.setField(service, "bateriaCritica", 20);
        ReflectionTestUtils.setField(service, "bateriaAtencao", 40);
        ReflectionTestUtils.setField(service, "combustivelCritico", 15);
        ReflectionTestUtils.setField(service, "combustivelAtencao", 30);
    }

    private Torre torre(int bateria, Integer combustivel) {
        Setor setor = new Setor("Setor Norte");
        Torre t = new Torre();
        t.setId(7L);
        t.setNome("Torre 07");
        t.setSetor(setor);
        t.setBateria(bateria);
        t.setCombustivel(combustivel);
        return t;
    }

    @Test
    void bateriaCriticaGeraAlertaCritico() {
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(i -> i.getArgument(0));

        service.verificarNiveis(torre(12, null));

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals(NivelAlerta.CRITICO, captor.getValue().getNivel());
        assertEquals(TipoAlerta.BATERIA_BAIXA, captor.getValue().getTipo());
        assertEquals("Bateria crítica — Torre 07 — Setor Norte", captor.getValue().getDescricao());
    }

    @Test
    void bateriaEntreOsLimitesGeraAlertaDeAtencao() {
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(i -> i.getArgument(0));

        service.verificarNiveis(torre(30, null));

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals(NivelAlerta.ATENCAO, captor.getValue().getNivel());
    }

    @Test
    void naoDuplicaAlertaAtivoDoMesmoTipo() {
        when(alertaRepository.existsByTorreIdAndTipoAndStatus(7L, TipoAlerta.BATERIA_BAIXA, StatusAlerta.ATIVO))
                .thenReturn(true);

        service.verificarNiveis(torre(5, null));

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void niveisNormaisNaoGeramAlerta() {
        service.verificarNiveis(torre(80, 70));

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void combustivelBaixoGeraAlertaDeCombustivel() {
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(i -> i.getArgument(0));

        service.verificarNiveis(torre(90, 10));

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals(TipoAlerta.COMBUSTIVEL_BAIXO, captor.getValue().getTipo());
        assertEquals(NivelAlerta.CRITICO, captor.getValue().getNivel());
    }
}
