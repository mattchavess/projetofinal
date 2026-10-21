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

## Arduino Uno real (LDR + 3 LEDs, via USB)

O Uno não tem Wi-Fi, então uma pequena **ponte** no computador liga a porta USB ao backend, usando o mesmo contrato acima. **Cada LED é uma torre**, com estado e modo próprios: LED do pino 10 = torre 1, pino 11 = torre 2, pino 12 = torre 3.

```
Arduino Uno --USB--> ponte-serial/ponte.py --HTTP--> backend (Docker, :8080) --> painel
```

**Montagem:** LEDs (com resistor de 220 ohms) nos pinos 10, 11 e 12; LDR em divisor de tensão com resistor de 10 kohms no A0 (escuro = leitura baixa).

**Passo a passo**
1. Grave `arduino-uno/arduino-uno.ino` no Uno (placa **Arduino Uno** selecionada) e **feche o Monitor Serial**.
2. Suba o sistema: `docker compose up` (na raiz do projeto).
3. Instale e rode a ponte. `--torres` lista os ids das torres do banco na ordem dos LEDs:
   ```bash
   pip install pyserial
   python ponte-serial/ponte.py --porta COM3 --torres 1,2,3
   ```
4. No painel, abra Monitoramento (atualiza a cada 10 s; a lista de Torres precisa de F5). Cubra o LDR: as três torres ligam e depois cada uma pode ser controlada separadamente. Clique em Ligar/Desligar ou troque o modo de uma torre e só o LED dela responde, em até 2 s.

**Modos = brilho do LED:** Econômico fraco, Normal médio, Intensidade máxima total, Cerração piscando. O pino 12 não tem PWM, então o brilho dele é gerado por software; para brilho mais estável, mova o LED 3 para o pino 9 ou 6 e ajuste o vetor `pinos` no sketch.

**Observações**
- A ponte roda no computador, fora do Docker (contêiner não enxerga a porta USB no Windows/Mac). O backend continua no Docker.
- Protocolo serial: Arduino envia `T,<torre>,<luz>,<0|1>`; a ponte envia `<torre>:<COMANDO>` (ex.: `2:LIGAR`).
- O LDR é um só: o escuro liga as 3 torres ao mesmo tempo. Um comando do painel vale para aquela torre até a próxima mudança claro/escuro.
- A luminosidade vai na escala bruta do Uno (0 a 1023). Os limites 75 e 100 ficam no topo do sketch.
- O Uno não mede bateria, então a ponte não envia esse campo e a bateria das torres não é alterada pela telemetria.
