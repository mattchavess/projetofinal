package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.ComandoGrupoRequest;
import br.com.minas.iluminacao.dto.ComandoGrupoResponse;
import br.com.minas.iluminacao.dto.TorreRequest;
import br.com.minas.iluminacao.evento.ComandoAplicadoEvento;
import br.com.minas.iluminacao.exception.RegraNegocioException;
import br.com.minas.iluminacao.model.Setor;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.*;
import br.com.minas.iluminacao.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TorreServiceTest {

    @Mock TorreRepository torreRepository;
    @Mock SetorRepository setorRepository;
    @Mock AlertaRepository alertaRepository;
    @Mock ManutencaoRepository manutencaoRepository;
    @Mock LeituraSensorRepository leituraRepository;
    @Mock HistoricoOperacaoRepository historicoRepository;
    @Mock AlertaService alertaService;
    @Mock HistoricoService historicoService;
    @Mock ApplicationEventPublisher eventos;
    @InjectMocks TorreService service;

    private Torre torre(long id, String nome, StatusTorre status) {
        Setor setor = new Setor("Setor Norte");
        setor.setId(1L);
        Torre t = new Torre();
        t.setId(id);
        t.setNome(nome);
        t.setCodigo("T" + id);
        t.setSetor(setor);
        t.setStatus(status);
        t.setFonteEnergia(FonteEnergia.SOLAR);
        return t;
    }

    @Test
    void naoCriaTorreComCodigoDuplicado() {
        when(torreRepository.existsByCodigo("TRM-001")).thenReturn(true);
        TorreRequest req = new TorreRequest("Torre 01", "TRM-001", null, null, null, 1L,
                FonteEnergia.SOLAR, StatusTorre.LIGADA, 80, null, ModoIluminacao.NORMAL, 400);

        assertThrows(RegraNegocioException.class, () -> service.criar(req));
        verify(torreRepository, never()).save(any());
    }

    @Test
    void comandoEmGrupoIgnoraTorreEmManutencao() {
        Torre ligada = torre(1, "Torre 01", StatusTorre.LIGADA);
        Torre manutencao = torre(2, "Torre 02", StatusTorre.EM_MANUTENCAO);
        when(torreRepository.findById(1L)).thenReturn(Optional.of(ligada));
        when(torreRepository.findById(2L)).thenReturn(Optional.of(manutencao));

        ComandoGrupoResponse r = service.executarComandoGrupo(
                new ComandoGrupoRequest(List.of(1L, 2L), ComandoTorre.CERRACAO));

        assertEquals(1, r.aplicadas());
        assertEquals(1, r.ignoradas().size());
        assertEquals(ModoIluminacao.CERRACAO, ligada.getModoIluminacao());
        assertEquals(ModoIluminacao.NORMAL, manutencao.getModoIluminacao());
        verify(eventos, times(1)).publishEvent(any(ComandoAplicadoEvento.class));
    }

    @Test
    void naoAlteraStatusDeTorreEmManutencao() {
        Torre t = torre(1, "Torre 01", StatusTorre.EM_MANUTENCAO);
        when(torreRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThrows(RegraNegocioException.class, () -> service.alterarStatus(1L, StatusTorre.LIGADA));
        assertEquals(StatusTorre.EM_MANUTENCAO, t.getStatus());
    }

    @Test
    void ligarAtualizaStatusERegistraHistorico() {
        Torre t = torre(1, "Torre 01", StatusTorre.DESLIGADA);
        when(torreRepository.findById(1L)).thenReturn(Optional.of(t));

        service.executarComando(1L, ComandoTorre.LIGAR);

        assertEquals(StatusTorre.LIGADA, t.getStatus());
        verify(historicoService).registrar(t, "Torre Torre 01 ligada.");
    }
}
