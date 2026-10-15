# Documentação da API

Base: `http://localhost:8080/api`. Corpo e resposta em JSON.

**Autenticação:** `Authorization: Bearer <token>` em todas as rotas, exceto `POST /auth/login` e `/iot/**` (esta usa `X-API-KEY`).

**Erros** (todos no mesmo formato, sem stack trace):
```json
{ "status": 404, "erro": "Recurso não encontrado", "mensagem": "Torre não encontrada", "timestamp": "2026-10-15T10:30:00" }
```

| Código | Quando |
|--------|--------|
| 200 | operação realizada |
| 201 | registro criado |
| 204 | sem conteúdo (exclusão; polling sem comando) |
| 400 | dados inválidos ou regra de negócio violada |
| 401 | sem login, token inválido ou chave de API inválida |
| 403 | sem permissão (perfil) |
| 404 | registro não encontrado |
| 500 | erro interno |

## Autenticação

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/auth/login` | `{"email","senha"}` devolve `{"token","nome","email","perfil"}` |
| GET | `/auth/me` | usuário logado |

## Torres

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/torres?busca=&status=&setorId=` | lista com pesquisa e filtros |
| GET | `/torres/{id}` | consulta |
| POST | `/torres` | cadastra (201) |
| PUT | `/torres/{id}` | edita |
| DELETE | `/torres/{id}` | exclui (204) |
| PATCH | `/torres/{id}/status` | `{"status":"LIGADA"}` |
| PATCH | `/torres/{id}/modo` | `{"modo":"CERRACAO"}` |
| POST | `/torres/{id}/comando` | `{"comando":"LIGAR"}` |
| POST | `/torres/comando-grupo` | `{"torreIds":[1,2],"comando":"CERRACAO"}` devolve `{"aplicadas":2,"ignoradas":[]}` |

Corpo de cadastro e edição:
```json
{
  "nome": "Torre 09", "codigo": "TRM-009", "localizacao": "Setor Norte - Frente 3",
  "latitude": -20.1, "longitude": -43.9, "setorId": 1,
  "fonteEnergia": "GERADOR", "status": "DESLIGADA",
  "bateria": 90, "combustivel": 70,
  "modoIluminacao": "NORMAL", "consumoWatts": 800
}
```
Valores: `fonteEnergia` = SOLAR, BATERIA, GERADOR. `status` = LIGADA, DESLIGADA, EM_MANUTENCAO, ALERTA, INDISPONIVEL. `modoIluminacao` = ECONOMICO, NORMAL, INTENSIDADE_MAXIMA, CERRACAO. Comandos: LIGAR, DESLIGAR e os quatro modos. Torre em manutenção recusa status e comandos (400).

## Setores

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/setores` | lista os setores |

## Alertas

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/alertas?status=&nivel=&torreId=` | lista (mais recentes primeiro) |
| POST | `/alertas` | alerta manual: `{"torreId":8,"tipo":"FALHA_TECNICA","nivel":"CRITICO","descricao":"..."}` (201) |
| PATCH | `/alertas/{id}/resolver` | marca como resolvido (400 se já estiver) |

Tipos: BATERIA_BAIXA, COMBUSTIVEL_BAIXO, FALHA_TECNICA, SEM_COMUNICACAO, MANUTENCAO_PENDENTE, CONSUMO_ELEVADO. Níveis: INFORMATIVO, ATENCAO, CRITICO. Situação: ATIVO, RESOLVIDO.

## Manutenções

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/manutencoes?status=&torreId=` | lista |
| GET | `/manutencoes/{id}` | consulta |
| POST | `/manutencoes` | abre (201); `"iniciar": true` já coloca a torre em manutenção |
| PUT | `/manutencoes/{id}` | edita; `status` = EM_ANDAMENTO, CONCLUIDA ou CANCELADA; `"devolverTorre": true` devolve a torre (Ligada) |

```json
{ "torreId": 8, "tipo": "CORRETIVA", "descricao": "Refletor queimado", "responsavel": "Equipe A",
  "dataPrevista": "2026-10-20", "observacoes": "", "iniciar": true }
```

## Dashboard e histórico

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/dashboard` | totais, consumo estimado (kW), horas de atividade, disponibilidade, torres por status, alertas por nível e tipo, consumo por setor |
| GET | `/historico?torreId=` | últimas 200 operações (`usuario` = "Sistema" quando automática) |

## Usuários (somente administrador)

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/usuarios` | lista |
| POST | `/usuarios` | cria (201): `{"nome","email","senha","perfil","ativo"}` |
| PUT | `/usuarios/{id}` | edita; senha em branco mantém a atual |

## IoT (cabeçalho `X-API-KEY`)

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/iot/telemetria` | `{"torreId":1,"luminosidade":320,"bateria":74,"status":"LIGADA"}` devolve `{"torreId","status","modo","comandoPendente"}` |
| GET | `/iot/torres/{id}/comando` | 200 `{"id","comando"}` ou 204 |

Mais detalhes em `iot/README.md`.
