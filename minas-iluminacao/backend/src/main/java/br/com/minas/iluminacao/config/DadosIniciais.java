package br.com.minas.iluminacao.config;

import br.com.minas.iluminacao.model.Setor;
import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.*;
import br.com.minas.iluminacao.repository.SetorRepository;
import br.com.minas.iluminacao.repository.TorreRepository;
import br.com.minas.iluminacao.service.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** Dados de demonstração. Só roda se o banco estiver vazio e app.seed.enabled=true. */
@Component
@RequiredArgsConstructor
public class DadosIniciais implements CommandLineRunner {

    private final SetorRepository setorRepository;
    private final TorreRepository torreRepository;
    private final AlertaService alertaService;

    @Value("${app.seed.enabled:true}")
    private boolean habilitado;

    @Override
    public void run(String... args) {
        if (!habilitado || setorRepository.count() > 0) return;

        List<Setor> setores = setorRepository.saveAll(List.of(
                new Setor("Setor Norte"), new Setor("Setor Sul"),
                new Setor("Setor Leste"), new Setor("Setor Oeste")));

        criar("Torre 01", "TRM-001", setores.get(0), FonteEnergia.SOLAR, StatusTorre.LIGADA, 88, null, ModoIluminacao.NORMAL, 400, 312.5);
        criar("Torre 02", "TRM-002", setores.get(0), FonteEnergia.BATERIA, StatusTorre.LIGADA, 12, null, ModoIluminacao.ECONOMICO, 350, 410.0);
        criar("Torre 03", "TRM-003", setores.get(1), FonteEnergia.GERADOR, StatusTorre.LIGADA, 95, 62, ModoIluminacao.NORMAL, 800, 520.0);
        criar("Torre 04", "TRM-004", setores.get(1), FonteEnergia.SOLAR, StatusTorre.DESLIGADA, 74, null, ModoIluminacao.NORMAL, 400, 120.0);
        criar("Torre 05", "TRM-005", setores.get(2), FonteEnergia.GERADOR, StatusTorre.LIGADA, 90, 10, ModoIluminacao.CERRACAO, 800, 640.0);
        criar("Torre 06", "TRM-006", setores.get(2), FonteEnergia.SOLAR, StatusTorre.INDISPONIVEL, 55, null, ModoIluminacao.NORMAL, 400, 80.0);
        criar("Torre 07", "TRM-007", setores.get(0), FonteEnergia.BATERIA, StatusTorre.LIGADA, 15, null, ModoIluminacao.NORMAL, 350, 275.0);
        criar("Torre 08", "TRM-008", setores.get(3), FonteEnergia.GERADOR, StatusTorre.EM_MANUTENCAO, 80, 45, ModoIluminacao.NORMAL, 800, 700.0);
    }

    private void criar(String nome, String codigo, Setor setor, FonteEnergia fonte, StatusTorre status,
                       int bateria, Integer combustivel, ModoIluminacao modo, int watts, double horas) {
        Torre t = new Torre();
        t.setNome(nome);
        t.setCodigo(codigo);
        t.setSetor(setor);
        t.setLocalizacao(setor.getNome() + " - Frente de lavra " + codigo.substring(4));
        t.setFonteEnergia(fonte);
        t.setStatus(status);
        t.setBateria(bateria);
        t.setCombustivel(combustivel);
        t.setModoIluminacao(modo);
        t.setConsumoWatts(watts);
        t.setHorasAtividade(horas);
        t.setUltimaComunicacao(LocalDateTime.now());
        t = torreRepository.save(t);
        alertaService.verificarNiveis(t);
    }
}
