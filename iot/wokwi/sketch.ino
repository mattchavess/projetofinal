/*
 * Torre de iluminação inteligente - ESP32 (simulação no Wokwi)
 *
 * LED  (GPIO 26) = iluminação da torre
 * LDR  (GPIO 34) = luminosidade do ambiente
 *
 * A cada ciclo o ESP32:
 *   1. lê o LDR e envia telemetria  -> POST /api/iot/telemetria
 *   2. consulta comandos pendentes  -> GET  /api/iot/torres/{id}/comando
 *   3. aplica o comando recebido no LED
 *
 * Escuro no ambiente liga a torre sozinha (e claro desliga), até que o operador
 * envie outro comando pelo painel.
 */
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>

// ======== AJUSTE AQUI ========
const char* WIFI_SSID = "Wokwi-GUEST";   // rede padrão do Wokwi
const char* WIFI_SENHA = "";
// O Wokwi NÃO enxerga o "localhost" do seu computador. Use um túnel público
// (ex.: ngrok http 8080) e cole a URL aqui, sem barra no final.
const String URL_API = "https://SEU-TUNEL.ngrok-free.app";
const char* API_KEY = "dev-iot-key";     // mesmo valor de IOT_API_KEY do backend
const int TORRE_ID = 1;                  // id da torre no banco
// =============================

const int PIN_LED = 26;
const int PIN_LDR = 34;
const int LIMITE_ESCURO = 300;           // luminosidade (0-1000) abaixo disso = escuro
const unsigned long INTERVALO_TELEMETRIA = 10000;
const unsigned long INTERVALO_POLLING = 2000;

// brilho do LED (PWM 0-255) por modo de iluminação
const int BRILHO_ECONOMICO = 60;
const int BRILHO_NORMAL = 140;
const int BRILHO_MAXIMO = 255;

bool ligada = false;
String modo = "NORMAL";
bool estavaEscuro = false;
int bateria = 100;
unsigned long ultimaTelemetria = 0, ultimoPolling = 0, ultimaBateria = 0;

WiFiClient clienteSimples;
WiFiClientSecure clienteSeguro;

bool iniciarHttp(HTTPClient& http, const String& url) {
  if (url.startsWith("https")) {
    clienteSeguro.setInsecure();  // simulação: não valida certificado
    return http.begin(clienteSeguro, url);
  }
  return http.begin(clienteSimples, url);
}

int lerLuminosidade() {
  return map(analogRead(PIN_LDR), 0, 4095, 0, 1000);
}

void aplicarLed() {
  int brilho = 0;
  if (ligada) {
    if (modo == "ECONOMICO") brilho = BRILHO_ECONOMICO;
    else if (modo == "INTENSIDADE_MAXIMA") brilho = BRILHO_MAXIMO;
    else brilho = BRILHO_NORMAL;
  }
  if (ligada && modo == "CERRACAO") {   // cerração: pisca para chamar atenção
    brilho = (millis() / 400) % 2 ? BRILHO_MAXIMO : 0;
  }
  ledcWrite(PIN_LED, brilho);
}

void conectarWifi() {
  Serial.print("Conectando ao Wi-Fi");
  WiFi.begin(WIFI_SSID, WIFI_SENHA);
  while (WiFi.status() != WL_CONNECTED) { delay(300); Serial.print("."); }
  Serial.println(" ok");
}

void enviarTelemetria() {
  int lum = lerLuminosidade();
  HTTPClient http;
  if (!iniciarHttp(http, URL_API + "/api/iot/telemetria")) return;
  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-API-KEY", API_KEY);
  http.addHeader("ngrok-skip-browser-warning", "true");

  String corpo = "{\"torreId\":" + String(TORRE_ID) +
                 ",\"luminosidade\":" + String(lum) +
                 ",\"bateria\":" + String(bateria) +
                 ",\"status\":\"" + (ligada ? "LIGADA" : "DESLIGADA") + "\"}";
  int codigo = http.POST(corpo);
  Serial.printf("[telemetria] lum=%d bateria=%d%% -> HTTP %d\n", lum, bateria, codigo);
  http.end();
}

// Extrai o valor de "comando":"XXXX" sem depender de biblioteca JSON.
String extrairComando(const String& json) {
  int i = json.indexOf("\"comando\":\"");
  if (i < 0) return "";
  i += 11;
  return json.substring(i, json.indexOf('"', i));
}

void consultarComando() {
  HTTPClient http;
  if (!iniciarHttp(http, URL_API + "/api/iot/torres/" + String(TORRE_ID) + "/comando")) return;
  http.addHeader("X-API-KEY", API_KEY);
  http.addHeader("ngrok-skip-browser-warning", "true");
  int codigo = http.GET();
  if (codigo == 200) {
    String comando = extrairComando(http.getString());
    Serial.println("[comando] recebido: " + comando);
    if (comando == "LIGAR") ligada = true;
    else if (comando == "DESLIGAR") ligada = false;
    else if (comando == "ECONOMICO" || comando == "NORMAL" ||
             comando == "INTENSIDADE_MAXIMA" || comando == "CERRACAO") modo = comando;
    aplicarLed();
  } else if (codigo != 204) {
    Serial.printf("[comando] HTTP %d\n", codigo);
  }
  http.end();
}

// Liga/desliga automaticamente quando o ambiente muda de claro para escuro (e vice-versa).
void verificarLuz() {
  bool escuro = lerLuminosidade() < LIMITE_ESCURO;
  if (escuro != estavaEscuro) {
    estavaEscuro = escuro;
    ligada = escuro;
    Serial.println(escuro ? "[luz] ambiente escuro: ligando torre" : "[luz] ambiente claro: desligando torre");
    aplicarLed();
    enviarTelemetria();  // avisa o backend na hora
  }
}

void setup() {
  Serial.begin(115200);
  ledcAttach(PIN_LED, 5000, 8);   // ESP32 Arduino core 3.x
  pinMode(PIN_LDR, INPUT);
  conectarWifi();
  estavaEscuro = lerLuminosidade() < LIMITE_ESCURO;
  ligada = estavaEscuro;
  aplicarLed();
  enviarTelemetria();  // primeira telemetria registra o dispositivo no backend
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) conectarWifi();
  unsigned long agora = millis();

  verificarLuz();
  if (modo == "CERRACAO") aplicarLed();

  // bateria simulada: perde 1% por minuto (mínimo 5%)
  if (agora - ultimaBateria > 60000) { ultimaBateria = agora; if (bateria > 5) bateria--; }

  if (agora - ultimaTelemetria >= INTERVALO_TELEMETRIA) { ultimaTelemetria = agora; enviarTelemetria(); }
  if (agora - ultimoPolling >= INTERVALO_POLLING) { ultimoPolling = agora; consultarComando(); }
  delay(50);
}
