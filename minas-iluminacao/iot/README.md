# Integração IoT

O módulo IoT é opcional: sem nenhum dispositivo conectado, o sistema funciona normalmente.

## Contrato com o dispositivo

Todas as chamadas levam o cabeçalho `X-API-KEY` (valor de `IOT_API_KEY`, padrão `dev-iot-key`). Sem ele, a resposta é 401.

**Telemetria**: `POST /api/iot/telemetria`
```json
{ "torreId": 1, "luminosidade": 320, "bateria": 74, "status": "LIGADA" }
```
Resposta 200: `{ "torreId": 1, "status": "LIGADA", "modo": "NORMAL", "comandoPendente": false }`.
Torre inexistente: 404. Dados inválidos: 400.

**Comando pendente (polling)**: `GET /api/iot/torres/{id}/comando`
- 200 `{ "id": 7, "comando": "LIGAR" }`: comando entregue e marcado como entregue.
- 204: nada pendente.

Comandos possíveis: `LIGAR, DESLIGAR, ECONOMICO, NORMAL, INTENSIDADE_MAXIMA, CERRACAO`.

## Regras

- A primeira telemetria "conecta" a torre. A partir daí, cada comando do painel (individual ou em grupo) entra na fila dessa torre. Torres sem telemetria não geram fila.
- O estado informado pelo dispositivo (`LIGADA`/`DESLIGADA`) atualiza a torre, **exceto** se houver comando pendente (evita que telemetria antiga desfaça o comando do operador) ou se a torre estiver em manutenção.
- A bateria informada atualiza a torre e pode gerar alerta de bateria baixa/crítica.
- Torre conectada que fique mais de 5 minutos sem telemetria recebe alerta "Sem comunicação" (verificação a cada 60 s). O alerta é resolvido sozinho quando a telemetria volta.
- Leituras com mais de 3 dias são apagadas automaticamente. Limites em `application.yml` (`app.iot`).

## Testar sem hardware

Com o sistema no ar: `./simular-esp32.sh 1` (id da torre). Depois, no painel, abra a torre 1 em Monitoramento e envie "Ligar" ou "Intensidade máxima": o comando aparece no polling do script.

## Simulação no Wokwi (ESP32 + LED + LDR)

1. Em https://wokwi.com crie um projeto ESP32 e cole `wokwi/sketch.ino` e `wokwi/diagram.json`. Se o Wokwi reclamar de algum pino no diagrama, religue manualmente: LED (via resistor de 220 ohms) no GPIO 26 e saída analógica (AO) do LDR no GPIO 34.
2. **O Wokwi não acessa o `localhost` do seu computador.** Exponha o backend com um túnel e use a URL pública no sketch (`URL_API`):
   ```bash
   ngrok http 8080        # ou: cloudflared tunnel --url http://localhost:8080
   ```
   O Wokwi também oferece um gateway privado para acessar a rede local; verifique na documentação deles se está disponível no seu plano.
3. Ajuste `TORRE_ID` e `API_KEY` no sketch. A rede Wi-Fi do simulador é `Wokwi-GUEST`.
4. Rode a simulação. O monitor serial mostra a telemetria e os comandos recebidos.

Fluxo de demonstração:
- Operador clica em **Ligar** no painel > Spring Boot registra e enfileira > o ESP32 consulta a API (a cada 2 s) > recebe `LIGAR` > o LED acende.
- Modos: Econômico, Normal e Intensidade máxima mudam o brilho do LED; Cerração faz o LED piscar.
- Ao baixar a luminosidade do LDR (arraste o controle no Wokwi), o ESP32 liga a torre e informa o backend; o painel passa a mostrar a torre como ligada.
- O módulo foi calibrado para `luminosidade < 300` = escuro (escala 0 a 1000). Observe o valor no monitor serial e ajuste `LIMITE_ESCURO` se o sensor simulado se comportar ao contrário.
