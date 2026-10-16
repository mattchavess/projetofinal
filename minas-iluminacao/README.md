# Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas

Aplicação web para gerenciar torres móveis de iluminação em áreas de mineração.
Stack: Java 17 + Spring Boot 3 + JPA/Hibernate + MySQL 8, Vue 3 (próximas fases), Docker Compose.

## Status do desenvolvimento

| Fase | Conteúdo | Status |
|------|----------|--------|
| 1 | Estrutura, Docker Compose, MySQL com volume | pronta |
| 2 | Entidades, relacionamentos, repositories | pronta |
| 3 | Services, controllers, DTOs, tratamento global de erros | pronta |
| 4 | Autenticação (JWT) e usuários | pronta |
| 5-9 | Frontend Vue (login, torres, dashboard, monitoramento, alertas, manutenções, histórico, usuários, controle individual e em grupo) | pronta |
| 10 | IoT (ESP32/Wokwi): telemetria, polling de comandos, sketch e diagrama | pronta |
| 11 | Testes automatizados e documentação | pronta |

## Como executar

```bash
cp .env.example .env      # opcional: os valores padrão já funcionam
docker compose up --build
```

- Painel (Vue): http://localhost:8081
- API: http://localhost:8080/api (exige login, exceto `POST /api/auth/login`)
- Usuários de demonstração: `admin@minas.com` / `admin123` (administrador) e `operador@minas.com` / `operador123`. Troque a senha do admin com `ADMIN_PASSWORD` no `.env` antes da primeira execução.
- MySQL (host): `localhost:3307` (usuário/senha em `.env`)
- Os dados do MySQL ficam no volume `mysql_data` e sobrevivem a reinicializações.
- Na primeira execução, o backend cria as tabelas e carrega 4 setores e 8 torres de demonstração.

Desenvolvimento do frontend com recarga automática: `cd frontend && npm install && npm run dev` (http://localhost:5173, com proxy de `/api` para `localhost:8080`).

Para rodar o backend fora do Docker: suba só o MySQL (`docker compose up mysql`) e, em `backend/`, rode `mvn spring-boot:run`.

## Autenticação

`POST /api/auth/login` com `{"email","senha"}` devolve um JWT. Envie `Authorization: Bearer <token>` nas demais rotas. `GET /api/auth/me` devolve o usuário logado. A área `/api/usuarios` é restrita a administradores. Sem token, a API responde 401; sem permissão, 403.

## Estrutura

```
backend/src/main/java/br/com/minas/iluminacao/
  controller/  requisições HTTP (sem regra de negócio)
  service/     regras de negócio
  repository/  Spring Data JPA
  model/       entidades e enums
  dto/         objetos de entrada e saída da API
  config/      CORS, dados iniciais
  exception/   exceções e handler global (respostas padronizadas)
```

## Endpoints (fase 3)

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | /api/torres?busca=&status=&setorId= | lista com pesquisa e filtros |
| GET | /api/torres/{id} | consulta |
| POST | /api/torres | cadastra (201) |
| PUT | /api/torres/{id} | edita |
| DELETE | /api/torres/{id} | exclui (204) |
| PATCH | /api/torres/{id}/status | `{"status":"LIGADA"}` |
| PATCH | /api/torres/{id}/modo | `{"modo":"CERRACAO"}` |
| POST | /api/torres/{id}/comando | `{"comando":"LIGAR"}` |
| POST | /api/torres/comando-grupo | `{"torreIds":[1,2],"comando":"CERRACAO"}` |
| GET | /api/setores | lista setores |
| GET | /api/alertas?status=&nivel=&torreId= | lista alertas |
| POST | /api/alertas | registra alerta manual (ex.: falha técnica) |
| PATCH | /api/alertas/{id}/resolver | resolve |
| GET | /api/manutencoes?status=&torreId= | lista |
| POST | /api/manutencoes | abre (use `"iniciar": true` para colocar a torre em manutenção) |
| PUT | /api/manutencoes/{id} | edita; `status: CONCLUIDA` + `devolverTorre: true` devolve a torre à operação |
| GET | /api/dashboard | indicadores e dados dos gráficos |
| GET | /api/historico?torreId= | últimas 200 operações |

Comandos: `LIGAR, DESLIGAR, ECONOMICO, NORMAL, INTENSIDADE_MAXIMA, CERRACAO`.

Regras já implementadas:
- Bateria abaixo de 40% gera alerta de atenção; abaixo de 20%, crítico. Combustível: 30% / 15%. Sem duplicar alerta ativo do mesmo tipo (limites em `application.yml`).
- Torre em manutenção rejeita comandos individuais e é ignorada nos comandos em grupo.
- Toda operação relevante vai para o histórico.
- Erros seguem o formato `{status, erro, mensagem, timestamp}`, sem stack trace.

## Frontend

```
frontend/src/
  views/       telas (Login, Dashboard, Torres, Monitoramento, Alertas, Manutenções, Histórico, Usuários, Configurações)
  components/  Modal, ConfirmDialog, TorreForm, TorrePainel, gráficos, selos
  services/    chamadas Axios à API (uma função por endpoint)
  router/      rotas com proteção por login e por perfil
  stores/      sessão do usuário
```

Roteiro de demonstração: Login > Dashboard (alertas críticos no topo) > Monitoramento > clicar na Torre 07 (bateria crítica) > Intensidade máxima (confirmação) > Registrar falha > Abrir manutenção corretiva (torre passa a "Em manutenção") > Dashboard atualizado > Histórico.

## Testes

```bash
cd backend && mvn test
```
- `service/`: testes unitários (Mockito) das regras de torres e alertas.
- `integracao/`: percorre a API inteira (login, CRUD, alertas, comandos, manutenção, dashboard e IoT) com H2 em modo MySQL, sem precisar do MySQL.

## IoT

Veja `iot/README.md`: contrato da API, simulador por curl (`iot/simular-esp32.sh`) e passo a passo do Wokwi (sketch e diagrama em `iot/wokwi/`).

## Documentação

Em `docs/`: requisitos funcionais e não funcionais, casos de uso, DER, diagramas de sequência (Mermaid, renderizam no GitHub) e referência da API.

## Git

Branches: `main` (estável), `develop` (integração), `feature/<funcionalidade>` e `test/<tipo>`. Fluxo: criar a feature a partir de `develop`, abrir merge em `develop` e, quando a versão estiver validada, mesclar em `main`.
