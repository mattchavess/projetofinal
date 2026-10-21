/*
 * Torres de iluminação inteligente - Arduino Uno + LDR + 3 LEDs
 *
 * Cada LED é UMA torre, com estado e modo próprios:
 *   LED pino 10 = torre 1 | LED pino 11 = torre 2 | LED pino 12 = torre 3
 * LDR (A0) = luminosidade do ambiente, compartilhada pelas 3 torres.
 *
 * O Uno não tem Wi-Fi: conversa por USB (serial 9600) com a "ponte"
 * (iot/ponte-serial/ponte.py), que repassa tudo ao backend Spring Boot.
 *
 * Arduino -> ponte (uma linha por mensagem):
 *   T,<torre 1-3>,<luminosidade 0-1023>,<1 se ligada | 0 se desligada>
 *
 * Ponte -> Arduino (uma linha por comando):
 *   <torre 1-3>:<COMANDO>   ex.: 2:LIGAR
 *   COMANDOS: LIGAR | DESLIGAR | ECONOMICO | NORMAL | INTENSIDADE_MAXIMA | CERRACAO
 *
 * Modos = brilho do LED: ECONOMICO fraco, NORMAL médio, INTENSIDADE_MAXIMA
 * total, CERRACAO pisca. Os pinos 10 e 11 têm PWM; o 12 não, então o brilho
 * dele é gerado por software (funciona, mas pode oscilar de leve durante o
 * envio serial; para brilho perfeito, mova o LED 3 para o pino 9 ou 6 e
 * ajuste o vetor "pinos").
 *
 * Automático: escurecer liga as 3 torres e clarear desliga (com histerese).
 * Um comando do painel vale para aquela torre até a próxima mudança claro/escuro.
 */

const int NUM_TORRES = 3;
const int pinos[NUM_TORRES] = {10, 11, 12};   // LED de cada torre
const int sensorLDR = A0;

// Limites com margem de segurança (histerese)
const int limiteLigar = 75;
const int limiteDesligar = 100;

const unsigned long INTERVALO_TELEMETRIA = 5000;

const int BRILHO_ECONOMICO = 60;
const int BRILHO_NORMAL = 140;
const int BRILHO_MAXIMO = 255;

enum Modo { M_ECONOMICO, M_NORMAL, M_MAXIMA, M_CERRACAO };

bool ligada[NUM_TORRES];
Modo modo[NUM_TORRES];
bool escuroAuto = false;          // estado claro/escuro com histerese
unsigned long ultimaTelemetria = 0;
String linhaSerial = "";

int lerLuz() {
  return analogRead(sensorLDR);
}

int brilhoDaTorre(int i) {
  if (!ligada[i]) return 0;
  switch (modo[i]) {
    case M_ECONOMICO: return BRILHO_ECONOMICO;
    case M_NORMAL:    return BRILHO_NORMAL;
    case M_MAXIMA:    return BRILHO_MAXIMO;
    case M_CERRACAO:  return ((millis() / 400) % 2) == 0 ? BRILHO_MAXIMO : 0;
  }
  return 0;
}

// Atualiza os LEDs. Chamada a cada volta do loop (necessário ao PWM por software do pino 12).
void aplicarLeds() {
  for (int i = 0; i < NUM_TORRES; i++) {
    int b = brilhoDaTorre(i);
    if (pinos[i] == 10 || pinos[i] == 11 || pinos[i] == 9 || pinos[i] == 6 ||
        pinos[i] == 5 || pinos[i] == 3) {
      analogWrite(pinos[i], b);                      // PWM de hardware
    } else {
      // PWM por software: período ~1 ms, 256 níveis
      bool aceso = b > 0 && (int)((micros() >> 2) & 0xFF) <= b;
      digitalWrite(pinos[i], aceso ? HIGH : LOW);
    }
  }
}

void enviarTelemetria(int i, int luz) {
  Serial.print("T,");
  Serial.print(i + 1);
  Serial.print(",");
  Serial.print(luz);
  Serial.print(",");
  Serial.println(ligada[i] ? 1 : 0);
}

void enviarTodas(int luz) {
  for (int i = 0; i < NUM_TORRES; i++) enviarTelemetria(i, luz);
}

// Formato "<torre>:<COMANDO>", ex.: "2:LIGAR"
void tratarComando(String cmd) {
  cmd.trim();
  cmd.toUpperCase();
  if (cmd.length() < 3 || cmd.charAt(1) != ':') return;
  int i = cmd.charAt(0) - '1';
  if (i < 0 || i >= NUM_TORRES) return;
  String c = cmd.substring(2);

  if (c == "LIGAR") ligada[i] = true;
  else if (c == "DESLIGAR") ligada[i] = false;
  else if (c == "ECONOMICO") modo[i] = M_ECONOMICO;
  else if (c == "NORMAL") modo[i] = M_NORMAL;
  else if (c == "INTENSIDADE_MAXIMA") modo[i] = M_MAXIMA;
  else if (c == "CERRACAO") modo[i] = M_CERRACAO;
  else return;                         // comando desconhecido: ignora

  enviarTelemetria(i, lerLuz());       // confirma o novo estado ao backend
}

void lerSerial() {
  while (Serial.available() > 0) {
    char ch = (char)Serial.read();
    if (ch == '\n') {
      tratarComando(linhaSerial);
      linhaSerial = "";
    } else if (ch != '\r' && linhaSerial.length() < 40) {
      linhaSerial += ch;
    }
  }
}

void setup() {
  for (int i = 0; i < NUM_TORRES; i++) {
    pinMode(pinos[i], OUTPUT);
    modo[i] = M_NORMAL;               // mesmo padrão do painel
  }
  Serial.begin(9600);

  int luz = lerLuz();
  escuroAuto = luz < limiteLigar;     // estado inicial conforme o ambiente
  for (int i = 0; i < NUM_TORRES; i++) ligada[i] = escuroAuto;
  aplicarLeds();
  enviarTodas(luz);
}

void loop() {
  int luz = lerLuz();

  // Histerese: só muda de estado ao cruzar os limites
  bool novoEscuro = escuroAuto;
  if (luz < limiteLigar) novoEscuro = true;
  else if (luz > limiteDesligar) novoEscuro = false;

  if (novoEscuro != escuroAuto) {
    escuroAuto = novoEscuro;
    for (int i = 0; i < NUM_TORRES; i++) ligada[i] = escuroAuto;  // a automação reassume
    aplicarLeds();
    enviarTodas(luz);                 // avisa o backend na hora
  }

  lerSerial();
  aplicarLeds();

  unsigned long agora = millis();
  if (agora - ultimaTelemetria >= INTERVALO_TELEMETRIA) {
    ultimaTelemetria = agora;
    enviarTodas(luz);
  }
}
