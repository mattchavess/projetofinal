#!/usr/bin/env python3
"""
Ponte USB-serial <-> backend, para o Arduino Uno (que não tem Wi-Fi).

  Arduino --"T,<n>,<luz>,<0|1>"--> ponte --POST /api/iot/telemetria--> backend
  Arduino <--"<n>:LIGAR" etc.----- ponte <--GET  /api/iot/torres/{id}/comando-- backend

Cada LED do Arduino é uma torre: LED n (1, 2, 3) corresponde ao n-ésimo id de --torres.

Uso:
  pip install pyserial
  python ponte.py --porta COM3 --torres 1,2,3
  python ponte.py --porta /dev/ttyACM0 --torres 1,2,3 --api http://localhost:8080 --chave dev-iot-key

Feche o Monitor Serial da IDE do Arduino antes: só um programa usa a porta por vez.
Rode a ponte no computador (fora do Docker); o backend pode estar no Docker normalmente.
"""
import argparse
import json
import time
import urllib.error
import urllib.request

COMANDOS_VALIDOS = {"LIGAR", "DESLIGAR", "ECONOMICO", "NORMAL", "INTENSIDADE_MAXIMA", "CERRACAO"}


def agora():
    return time.strftime("%H:%M:%S")


class Ponte:
    def __init__(self, serial, api, torres, chave, intervalo_polling=2.0, intervalo_envio=5.0):
        self.serial = serial
        self.api = api.rstrip("/")
        self.torres = list(torres)          # posição 0 = LED 1, posição 1 = LED 2, ...
        self.chave = chave
        self.intervalo_polling = intervalo_polling
        self.intervalo_envio = intervalo_envio
        self.ultimo_polling = 0.0
        self.ultimo_envio = {}              # torre_id -> instante do último envio
        self.ultimo_estado = {}             # torre_id -> ligada no último envio

    # ---------- HTTP ----------
    def _requisicao(self, caminho, corpo=None):
        dados = json.dumps(corpo).encode() if corpo is not None else None
        req = urllib.request.Request(self.api + caminho, data=dados, method="POST" if corpo is not None else "GET")
        req.add_header("X-API-KEY", self.chave)
        if dados is not None:
            req.add_header("Content-Type", "application/json")
        with urllib.request.urlopen(req, timeout=5) as resp:
            texto = resp.read().decode()
            return resp.status, (json.loads(texto) if texto else None)

    def enviar_telemetria(self, torre, luz, ligada):
        # "bateria" é omitida de propósito: o Uno não mede bateria e o backend a aceita nula.
        corpo = {"torreId": torre, "luminosidade": luz, "status": "LIGADA" if ligada else "DESLIGADA"}
        try:
            status, _ = self._requisicao("/api/iot/telemetria", corpo)
            print(f"[{agora()}] torre {torre}: luz={luz} ligada={ligada} -> HTTP {status}")
        except urllib.error.HTTPError as e:
            print(f"[{agora()}] torre {torre}: telemetria recusada: HTTP {e.code} (torre existe? chave correta?)")
        except OSError as e:
            print(f"[{agora()}] backend inacessível: {e}")

    def buscar_comando(self, torre):
        try:
            status, corpo = self._requisicao(f"/api/iot/torres/{torre}/comando")
        except urllib.error.HTTPError as e:
            print(f"[{agora()}] torre {torre}: polling recusado: HTTP {e.code}")
            return None
        except OSError as e:
            print(f"[{agora()}] backend inacessível: {e}")
            return None
        if status == 200 and corpo:
            return corpo.get("comando")
        return None

    # ---------- Serial ----------
    def tratar_linha(self, linha):
        linha = linha.strip()
        if not linha.startswith("T,"):
            if linha:
                print(f"[{agora()}] arduino: {linha}")
            return
        try:
            _, n, luz, ligada = linha.split(",")
            n, luz, ligada = int(n), int(luz), ligada == "1"
            if n < 1:
                return
            torre = self.torres[n - 1]
        except (ValueError, IndexError):
            return  # linha cortada no início da conexão, ou LED sem torre configurada
        mudou = ligada != self.ultimo_estado.get(torre)
        if mudou or time.time() - self.ultimo_envio.get(torre, 0.0) >= self.intervalo_envio:
            self.ultimo_envio[torre] = time.time()
            self.ultimo_estado[torre] = ligada
            self.enviar_telemetria(torre, luz, ligada)

    def passo(self):
        """Um ciclo: lê o que o Arduino mandou e, de tempos em tempos, consulta comandos."""
        while self.serial.in_waiting:
            self.tratar_linha(self.serial.readline().decode(errors="ignore"))
        if time.time() - self.ultimo_polling >= self.intervalo_polling:
            self.ultimo_polling = time.time()
            for n, torre in enumerate(self.torres, start=1):
                comando = self.buscar_comando(torre)
                if comando in COMANDOS_VALIDOS:
                    print(f"[{agora()}] torre {torre}: comando do painel {comando} -> LED {n}")
                    self.serial.write(f"{n}:{comando}\n".encode())

    def executar(self):
        while True:
            self.passo()
            time.sleep(0.05)


def main():
    p = argparse.ArgumentParser(description="Ponte serial do Arduino Uno para o backend de iluminação")
    p.add_argument("--porta", required=True, help="COM3 (Windows) ou /dev/ttyACM0 (Linux/Mac)")
    p.add_argument("--torres", default="1,2,3",
                   help="ids das torres no banco, na ordem dos LEDs (LED 1, LED 2, LED 3)")
    p.add_argument("--api", default="http://localhost:8080")
    p.add_argument("--chave", default="dev-iot-key", help="mesmo valor de IOT_API_KEY")
    a = p.parse_args()

    import serial  # pip install pyserial
    porta = serial.Serial(a.porta, 9600, timeout=1)
    time.sleep(2)  # o Uno reinicia ao abrir a porta
    torres = [int(x) for x in a.torres.split(",")]
    print(f"Ponte ativa: {a.porta} <-> {a.api} (LEDs 1..{len(torres)} = torres {torres}). Ctrl+C para sair.")
    try:
        Ponte(porta, a.api, torres, a.chave).executar()
    except KeyboardInterrupt:
        print("\nEncerrado.")


if __name__ == "__main__":
    main()
