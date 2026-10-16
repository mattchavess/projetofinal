#!/usr/bin/env bash
# Simula o ESP32 com curl, para testar a integração sem Wokwi.
# Uso: ./simular-esp32.sh [torreId] [urlApi] [chave]
TORRE=${1:-1}
API=${2:-http://localhost:8080}
CHAVE=${3:-dev-iot-key}

echo "== 1. Telemetria (registra o dispositivo, luminosidade baixa, bateria 74%) =="
curl -s -X POST "$API/api/iot/telemetria" -H "Content-Type: application/json" -H "X-API-KEY: $CHAVE" \
  -d "{\"torreId\":$TORRE,\"luminosidade\":120,\"bateria\":74,\"status\":\"LIGADA\"}"
echo -e "\n"

echo "== 2. Sem chave de API (deve ser 401) =="
curl -s -o /dev/null -w "HTTP %{http_code}\n" "$API/api/iot/torres/$TORRE/comando"
echo

echo "== 3. Polling de comandos (a cada 2 s por 30 s). Envie comandos pelo painel agora =="
for i in $(seq 1 15); do
  RESP=$(curl -s -w " [HTTP %{http_code}]" "$API/api/iot/torres/$TORRE/comando" -H "X-API-KEY: $CHAVE")
  echo "$(date +%T) $RESP"
  sleep 2
done
