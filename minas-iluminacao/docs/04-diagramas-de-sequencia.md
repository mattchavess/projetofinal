# Diagramas de Sequência

## 1. Operador comanda uma torre (fluxo principal)

Operador > Vue.js > Spring Boot > Service > Repository > MySQL.

```mermaid
sequenceDiagram
    actor Op as Operador
    participant Vue as Vue.js (TorrePainel)
    participant Api as TorreController
    participant Svc as TorreService
    participant Rep as TorreRepository / HistoricoService
    participant DB as MySQL

    Op->>Vue: clica em "Intensidade máxima"
    Vue->>Op: "Confirmar ativação do modo de intensidade máxima na Torre 04?"
    Op->>Vue: confirma
    Vue->>Api: POST /api/torres/4/comando {INTENSIDADE_MAXIMA} + Bearer token
    Note over Api: JwtAuthFilter valida o token
    Api->>Svc: executarComando(4, INTENSIDADE_MAXIMA)
    Svc->>Rep: findById(4)
    Rep->>DB: SELECT torre
    DB-->>Rep: torre
    Svc->>Svc: valida (torre não está em manutenção) e aplica o modo
    Svc->>Rep: save(torre)
    Rep->>DB: UPDATE torre
    Svc->>Rep: historicoService.registrar(torre, "...intensidade máxima.")
    Rep->>DB: INSERT historico_operacao (usuário logado)
    Svc-->>Api: TorreResponse
    Api-->>Vue: 200 OK + torre atualizada
    Vue-->>Op: mensagem de sucesso e painel atualizado
```

## 2. Login

```mermaid
sequenceDiagram
    actor Op as Operador
    participant Vue as Vue.js (Login)
    participant Api as AuthController
    participant Svc as AuthService
    participant DB as MySQL

    Op->>Vue: informa e-mail e senha
    Vue->>Api: POST /api/auth/login
    Api->>Svc: login(req)
    Svc->>DB: SELECT usuario WHERE email
    Svc->>Svc: confere BCrypt e se está ativo
    alt credenciais válidas
        Svc-->>Api: token JWT
        Api-->>Vue: 200 {token, nome, perfil}
        Vue->>Vue: guarda a sessão e vai para /dashboard
    else inválidas
        Api-->>Vue: 401 {mensagem}
    end
```

## 3. IoT: ESP32 envia telemetria

ESP32 > API Spring Boot > Banco de Dados.

```mermaid
sequenceDiagram
    participant Esp as ESP32
    participant Key as IotApiKeyFilter
    participant Api as IotController
    participant Svc as TelemetriaService
    participant Alt as AlertaService
    participant DB as MySQL

    Esp->>Key: POST /api/iot/telemetria {torreId, luminosidade, bateria, status} + X-API-KEY
    alt chave inválida
        Key-->>Esp: 401
    else chave válida
        Key->>Api: segue
        Api->>Svc: registrar(req)
        Svc->>DB: SELECT torre
        alt torre não existe
            Svc-->>Esp: 404
        else existe
            Svc->>DB: INSERT leitura_sensor
            Svc->>DB: UPDATE torre (bateria, última comunicação, estado se permitido)
            Svc->>Alt: verificarNiveis(torre) e resolver "sem comunicação"
            Alt->>DB: INSERT/UPDATE alerta
            Svc-->>Esp: 200 {status, modo, comandoPendente}
        end
    end
```

## 4. Caminho inverso: operador liga a torre e o LED acende

```mermaid
sequenceDiagram
    actor Op as Operador
    participant Vue as Vue.js
    participant Api as Spring Boot (TorreService)
    participant Ev as Evento ComandoAplicado
    participant Fila as ComandoPendenteService
    participant DB as MySQL
    participant Esp as ESP32
    participant Led as LED

    Op->>Vue: "Ligar torre" (confirma)
    Vue->>Api: POST /api/torres/1/comando {LIGAR}
    Api->>DB: UPDATE torre status=LIGADA + INSERT historico
    Api->>Ev: publica ComandoAplicadoEvento
    Ev->>Fila: aoAplicarComando
    Fila->>DB: INSERT comando_pendente (se a torre tem dispositivo)
    Api-->>Vue: 200
    loop a cada 2 s
        Esp->>Api: GET /api/iot/torres/1/comando + X-API-KEY
        alt há comando
            Api->>DB: marca entregue
            Api-->>Esp: 200 {"comando":"LIGAR"}
            Esp->>Led: acende
        else nada pendente
            Api-->>Esp: 204
        end
    end
    Esp->>Api: telemetria status=LIGADA (confirma o novo estado)
```

## 5. Falha técnica e manutenção corretiva

```mermaid
sequenceDiagram
    actor Op as Operador
    participant Vue as Vue.js
    participant Api as Spring Boot
    participant DB as MySQL

    Op->>Vue: "Registrar falha" na Torre 08
    Vue->>Api: POST /api/alertas {FALHA_TECNICA, CRITICO}
    Api->>DB: INSERT alerta + historico
    Op->>Vue: "Abrir manutenção corretiva"
    Vue->>Api: POST /api/manutencoes {CORRETIVA, iniciar: true}
    Api->>DB: INSERT manutencao (EM_ANDAMENTO)
    Api->>DB: UPDATE torre status=EM_MANUTENCAO + INSERT historico
    Api-->>Vue: 201
    Vue->>Api: GET /api/dashboard
    Api->>DB: contagens por status, alertas, consumo
    Api-->>Vue: indicadores atualizados
```
