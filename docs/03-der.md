# Modelo de Dados (DER)

```mermaid
erDiagram
    SETOR ||--o{ TORRE : possui
    TORRE ||--o{ ALERTA : gera
    TORRE ||--o{ MANUTENCAO : recebe
    TORRE ||--o{ HISTORICO_OPERACAO : registra
    TORRE ||--o{ LEITURA_SENSOR : envia
    TORRE ||--o{ COMANDO_PENDENTE : aguarda
    USUARIO ||--o{ HISTORICO_OPERACAO : executa

    SETOR {
        bigint id PK
        varchar nome UK
    }
    USUARIO {
        bigint id PK
        varchar nome
        varchar email UK
        varchar senha_hash
        varchar perfil "ADMIN | OPERADOR"
        boolean ativo
    }
    TORRE {
        bigint id PK
        varchar nome
        varchar codigo UK
        varchar localizacao
        double latitude
        double longitude
        bigint setor_id FK
        varchar fonte_energia "SOLAR | BATERIA | GERADOR"
        varchar status "LIGADA | DESLIGADA | EM_MANUTENCAO | ALERTA | INDISPONIVEL"
        int bateria
        int combustivel "nulo se nao for gerador"
        varchar modo_iluminacao
        int consumo_watts
        double horas_atividade
        datetime data_cadastro
        datetime ultima_atualizacao
        datetime ultima_comunicacao
    }
    ALERTA {
        bigint id PK
        bigint torre_id FK
        varchar tipo
        varchar nivel "INFORMATIVO | ATENCAO | CRITICO"
        varchar descricao
        datetime data_hora
        varchar status "ATIVO | RESOLVIDO"
        datetime data_resolucao
    }
    MANUTENCAO {
        bigint id PK
        bigint torre_id FK
        varchar tipo "PREVENTIVA | CORRETIVA"
        varchar descricao
        varchar responsavel
        datetime data_abertura
        date data_prevista
        datetime data_conclusao
        varchar status
        varchar observacoes
    }
    HISTORICO_OPERACAO {
        bigint id PK
        bigint torre_id FK "pode ficar nulo"
        varchar torre_nome
        bigint usuario_id FK "nulo = Sistema"
        varchar operacao
        datetime data_hora
    }
    LEITURA_SENSOR {
        bigint id PK
        bigint torre_id FK
        int luminosidade
        int bateria
        datetime data_hora
    }
    COMANDO_PENDENTE {
        bigint id PK
        bigint torre_id FK
        varchar comando
        datetime criado_em
        boolean entregue
        datetime entregue_em
    }
```

## Decisões

- **`HISTORICO_OPERACAO.torre_nome`** guarda o nome no momento da operação. Assim o histórico continua legível mesmo que a torre seja excluída (nesse caso `torre_id` fica nulo).
- **`usuario_id` nulo** indica operação feita pelo sistema (alertas automáticos, telemetria).
- **`consumo_watts` e `horas_atividade`** permitem estimar consumo e tempo de atividade no Dashboard sem tabela extra.
- **`COMANDO_PENDENTE`** é a fila do polling do ESP32; ela é criada somente para torres que já enviaram telemetria.
- O esquema é gerado pelo Hibernate (`ddl-auto: update`). Em produção, o ideal é adotar migrações (Flyway).
