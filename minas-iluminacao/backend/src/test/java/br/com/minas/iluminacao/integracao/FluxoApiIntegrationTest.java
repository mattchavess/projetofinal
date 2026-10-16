package br.com.minas.iluminacao.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Percorre a API de ponta a ponta (controller, service, repository e banco H2 em modo MySQL). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoApiIntegrationTest {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    private static final String CHAVE_IOT = "dev-iot-key";
    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    // ---------- auxiliares ----------

    private String login(String email, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login").contentType(JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(corpo).get("token").asText();
    }

    private String admin() throws Exception { return login("admin@minas.com", "admin123"); }

    private String jsonTorre(String codigo, String status, int bateria) {
        return "{\"nome\":\"Torre " + codigo + "\",\"codigo\":\"" + codigo + "\",\"setorId\":1,"
                + "\"fonteEnergia\":\"SOLAR\",\"status\":\"" + status + "\",\"bateria\":" + bateria
                + ",\"modoIluminacao\":\"NORMAL\",\"consumoWatts\":400}";
    }

    /** Cria uma torre pela API e devolve o id. */
    private long criarTorre(String token, String status, int bateria) throws Exception {
        String codigo = "TST-" + SEQ.incrementAndGet();
        String corpo = mvc.perform(post("/api/torres").header("Authorization", token).contentType(JSON)
                        .content(jsonTorre(codigo, status, bateria)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(corpo).get("id").asLong();
    }

    // ---------- autenticação ----------

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/api/torres"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(JSON)
                        .content("{\"email\":\"admin@minas.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
    }

    @Test
    void operadorNaoAcessaGestaoDeUsuarios() throws Exception {
        String token = login("operador@minas.com", "operador123");
        mvc.perform(get("/api/usuarios").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/torres").header("Authorization", token)).andExpect(status().isOk());
    }

    // ---------- torres ----------

    @Test
    void crudDeTorre() throws Exception {
        String token = admin();
        long id = criarTorre(token, "DESLIGADA", 90);

        mvc.perform(get("/api/torres/" + id).header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.setor").value("Setor Norte"));

        mvc.perform(put("/api/torres/" + id).header("Authorization", token).contentType(JSON)
                        .content(jsonTorre("EDIT-" + id, "LIGADA", 85)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("LIGADA"));

        mvc.perform(delete("/api/torres/" + id).header("Authorization", token)).andExpect(status().isNoContent());

        mvc.perform(get("/api/torres/" + id).header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Torre não encontrada"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void codigoDuplicadoRetorna400() throws Exception {
        mvc.perform(post("/api/torres").header("Authorization", admin()).contentType(JSON)
                        .content(jsonTorre("TRM-001", "LIGADA", 80)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dadosInvalidosRetornam400() throws Exception {
        mvc.perform(post("/api/torres").header("Authorization", admin()).contentType(JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados inválidos"));
    }

    @Test
    void listaFiltraPorStatusEBusca() throws Exception {
        String token = admin();
        mvc.perform(get("/api/torres").param("status", "EM_MANUTENCAO").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("EM_MANUTENCAO"))));
        mvc.perform(get("/api/torres").param("busca", "trm-003").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigo").value("TRM-003"));
    }

    // ---------- alertas, comandos e histórico ----------

    @Test
    void bateriaCriticaGeraAlertaQueNaoSeDuplicaEPodeSerResolvido() throws Exception {
        String token = admin();
        long id = criarTorre(token, "LIGADA", 10);

        mvc.perform(get("/api/alertas").param("torreId", String.valueOf(id)).param("status", "ATIVO")
                        .header("Authorization", token))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nivel").value("CRITICO"))
                .andExpect(jsonPath("$[0].descricao", startsWith("Bateria crítica — Torre TST-")));

        String lista = mvc.perform(get("/api/alertas").param("torreId", String.valueOf(id)).header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        long alertaId = mapper.readTree(lista).get(0).get("id").asLong();

        mvc.perform(patch("/api/alertas/" + alertaId + "/resolver").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVIDO"));
        mvc.perform(patch("/api/alertas/" + alertaId + "/resolver").header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void comandoAlteraModoERegistraHistoricoComUsuario() throws Exception {
        String token = admin();
        long id = criarTorre(token, "LIGADA", 90);

        mvc.perform(post("/api/torres/" + id + "/comando").header("Authorization", token).contentType(JSON)
                        .content("{\"comando\":\"INTENSIDADE_MAXIMA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.modoIluminacao").value("INTENSIDADE_MAXIMA"));

        mvc.perform(get("/api/historico").param("torreId", String.valueOf(id)).header("Authorization", token))
                .andExpect(jsonPath("$[0].operacao", containsString("intensidade máxima")))
                .andExpect(jsonPath("$[0].usuario").value("Administrador"));
    }

    @Test
    void comandoEmGrupoAtualizaTodasAsTorres() throws Exception {
        String token = admin();
        long a = criarTorre(token, "LIGADA", 90);
        long b = criarTorre(token, "LIGADA", 90);

        mvc.perform(post("/api/torres/comando-grupo").header("Authorization", token).contentType(JSON)
                        .content("{\"torreIds\":[" + a + "," + b + "],\"comando\":\"CERRACAO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.aplicadas").value(2));

        mvc.perform(get("/api/torres/" + b).header("Authorization", token))
                .andExpect(jsonPath("$.modoIluminacao").value("CERRACAO"));
    }

    // ---------- manutenção ----------

    @Test
    void manutencaoColocaTorreEmManutencaoEDevolveAoConcluir() throws Exception {
        String token = admin();
        long id = criarTorre(token, "LIGADA", 90);

        String corpo = mvc.perform(post("/api/manutencoes").header("Authorization", token).contentType(JSON)
                        .content("{\"torreId\":" + id + ",\"tipo\":\"CORRETIVA\",\"descricao\":\"Refletor queimado\",\"iniciar\":true}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andReturn().getResponse().getContentAsString();
        long manutId = mapper.readTree(corpo).get("id").asLong();

        mvc.perform(get("/api/torres/" + id).header("Authorization", token))
                .andExpect(jsonPath("$.status").value("EM_MANUTENCAO"));

        // torre em manutenção recusa comandos
        mvc.perform(post("/api/torres/" + id + "/comando").header("Authorization", token).contentType(JSON)
                        .content("{\"comando\":\"LIGAR\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/manutencoes/" + manutId).header("Authorization", token).contentType(JSON)
                        .content("{\"torreId\":" + id + ",\"tipo\":\"CORRETIVA\",\"descricao\":\"Refletor queimado\","
                                + "\"status\":\"CONCLUIDA\",\"devolverTorre\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONCLUIDA"));

        mvc.perform(get("/api/torres/" + id).header("Authorization", token))
                .andExpect(jsonPath("$.status").value("LIGADA"));
    }

    // ---------- dashboard ----------

    @Test
    void dashboardUsaDadosReaisDoBanco() throws Exception {
        mvc.perform(get("/api/dashboard").header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTorres", greaterThanOrEqualTo(8)))
                .andExpect(jsonPath("$.torresPorStatus.LIGADA").exists())
                .andExpect(jsonPath("$.alertasPorNivel.CRITICO").exists())
                .andExpect(jsonPath("$.consumoPorSetorKw").isMap());
    }

    // ---------- IoT ----------

    @Test
    void iotExigeChaveDeApi() throws Exception {
        mvc.perform(post("/api/iot/telemetria").contentType(JSON).content("{\"torreId\":1}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/iot/torres/1/comando").header("X-API-KEY", "errada"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void iotValidaTorreInexistente() throws Exception {
        mvc.perform(post("/api/iot/telemetria").header("X-API-KEY", CHAVE_IOT).contentType(JSON)
                        .content("{\"torreId\":999999,\"luminosidade\":100,\"bateria\":50,\"status\":\"LIGADA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void comandoDoOperadorChegaAoDispositivoSemSerDesfeitoPelaTelemetriaAntiga() throws Exception {
        String token = admin();
        long id = criarTorre(token, "DESLIGADA", 90);
        String telemetriaAntiga = "{\"torreId\":" + id + ",\"luminosidade\":500,\"bateria\":80,\"status\":\"DESLIGADA\"}";

        // 1. primeira telemetria registra o dispositivo; sem comando pendente
        mvc.perform(post("/api/iot/telemetria").header("X-API-KEY", CHAVE_IOT).contentType(JSON).content(telemetriaAntiga))
                .andExpect(status().isOk()).andExpect(jsonPath("$.comandoPendente").value(false));
        mvc.perform(get("/api/iot/torres/" + id + "/comando").header("X-API-KEY", CHAVE_IOT))
                .andExpect(status().isNoContent());

        // 2. operador liga a torre pelo painel
        mvc.perform(post("/api/torres/" + id + "/comando").header("Authorization", token).contentType(JSON)
                        .content("{\"comando\":\"LIGAR\"}"))
                .andExpect(status().isOk());

        // 3. telemetria ainda com o estado antigo não pode desfazer o comando
        mvc.perform(post("/api/iot/telemetria").header("X-API-KEY", CHAVE_IOT).contentType(JSON).content(telemetriaAntiga))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("LIGADA"))
                .andExpect(jsonPath("$.comandoPendente").value(true));

        // 4. dispositivo busca o comando: entregue uma vez só
        mvc.perform(get("/api/iot/torres/" + id + "/comando").header("X-API-KEY", CHAVE_IOT))
                .andExpect(status().isOk()).andExpect(jsonPath("$.comando").value("LIGAR"));
        mvc.perform(get("/api/iot/torres/" + id + "/comando").header("X-API-KEY", CHAVE_IOT))
                .andExpect(status().isNoContent());

        // 5. a bateria informada pelo dispositivo atualiza a torre
        mvc.perform(get("/api/torres/" + id).header("Authorization", token))
                .andExpect(jsonPath("$.bateria").value(80))
                .andExpect(jsonPath("$.ultimaComunicacao").exists());
    }

    @Test
    void torreSemDispositivoNaoGeraFilaDeComandos() throws Exception {
        String token = admin();
        long id = criarTorre(token, "DESLIGADA", 90);
        mvc.perform(post("/api/torres/" + id + "/comando").header("Authorization", token).contentType(JSON)
                        .content("{\"comando\":\"LIGAR\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/iot/torres/" + id + "/comando").header("X-API-KEY", CHAVE_IOT))
                .andExpect(status().isNoContent());
    }
}
