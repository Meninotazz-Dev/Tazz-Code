"""
O SERVIDOR DO TAZZHELPER (a IA própria do TazzCode, feita em Python).

O TazzCode (Java) conversa com ele pela "porta" 7878 do próprio computador:
    GET  /saude      -> {"ok": true, "miniIA": true/false}
    POST /perguntar  -> manda {"pergunta", "contexto", "historico"} e recebe a resposta aos pedacinhos
    POST /desligar   -> desliga tudo

Quem responde? A IA é HÍBRIDA:
    1) o ESPECIALISTA tenta primeiro (erros, conceitos, explicar código, dicas) — quase nunca erra
    2) se ele não souber, a MINI IA (Gemma 3 270M) responde — conversa sobre tudo, mas pode errar

Rodar na mão:  python servidor.py --pasta C:\\caminho\\da\\ia
"""
import argparse
import json
import os
import sys
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tazzhelper.especialista import Especialista   # noqa: E402
from tazzhelper.mini_ia import MiniIA, instrucoes  # noqa: E402

VERSAO = "1.0"
PORTA = 7878

especialista = Especialista()
mini_ia = None


class Atendente(BaseHTTPRequestHandler):

    def log_message(self, *args):   # não enche o console de mensagens
        pass

    def _json(self, codigo, dados):
        corpo = json.dumps(dados).encode("utf-8")
        self.send_response(codigo)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(corpo)))
        self.end_headers()
        self.wfile.write(corpo)

    def do_GET(self):
        if self.path == "/saude":
            self._json(200, {"ok": True, "versao": VERSAO, "miniIA": mini_ia.instalada()})
        else:
            self._json(404, {"erro": "não existe"})

    def do_POST(self):
        tamanho = int(self.headers.get("Content-Length", 0))
        try:
            pedido = json.loads(self.rfile.read(tamanho).decode("utf-8") or "{}")
        except ValueError:
            self._json(400, {"erro": "JSON inválido"})
            return
        if self.path == "/desligar":
            self._json(200, {"ok": True})
            threading.Thread(target=desligar_tudo, daemon=True).start()
        elif self.path == "/perguntar":
            self.perguntar(pedido)
        else:
            self._json(404, {"erro": "não existe"})

    # ------------------------------------------------------------------

    def _mandar(self, dados):
        """Manda um pedacinho no formato SSE (data: {...})."""
        self.wfile.write(("data: " + json.dumps(dados, ensure_ascii=False) + "\n\n").encode("utf-8"))
        self.wfile.flush()

    def perguntar(self, pedido):
        pergunta = pedido.get("pergunta", "")
        ctx = pedido.get("contexto", {}) or {}
        historico = pedido.get("historico", []) or []

        self.send_response(200)
        self.send_header("Content-Type", "text/event-stream; charset=utf-8")
        self.send_header("Cache-Control", "no-cache")
        self.end_headers()

        # 1) O especialista tenta primeiro
        resposta, certeza = especialista.responder(pergunta, ctx)
        if certeza >= 0.6:
            for i in range(0, len(resposta), 12):   # "digita" aos pouquinhos
                self._mandar({"texto": resposta[i:i + 12]})
                time.sleep(0.008)
            self._mandar({"fim": True, "quem": "especialista"})
            return

        # 2) A mini IA
        if mini_ia.instalada():
            self._mandar({"aviso": "pensando com a mini IA..."})
            if not mini_ia.ligar():
                self._mandar({"texto": "Não consegui ligar a mini IA. Tente reinstalar ela em Configurar."})
                self._mandar({"fim": True, "quem": "erro"})
                return
            mensagens = [{"role": "system", "content": instrucoes(ctx)}]
            for h in historico[-6:]:
                mensagens.append({"role": "assistant" if h.get("papel") == "assistant" else "user", "content": h.get("texto", "")[:1500]})
            mensagens.append({"role": "user", "content": pergunta})
            try:
                for pedaco in mini_ia.conversar(mensagens):
                    self._mandar({"texto": pedaco})
            except Exception as e:   # noqa: BLE001
                self._mandar({"texto": f"\n\n(a mini IA teve um problema: {e})"})
            self._mandar({"fim": True, "quem": "mini-ia"})
            return

        # 3) Ninguém sabe
        texto = resposta or ("Essa eu ainda não sei! Minha cabeça especialista só conhece programação "
                             "(erros, conceitos, explicar código, dicas).\n\nPra conversar sobre qualquer assunto, "
                             "instale a **mini IA** (Gemma, ~250 MB) em **Configurar**.")
        self._mandar({"texto": texto})
        self._mandar({"fim": True, "quem": "especialista"})


servidor = None


def desligar_tudo():
    time.sleep(0.2)
    if mini_ia:
        mini_ia.desligar()
    if servidor:
        servidor.shutdown()


def main():
    global mini_ia, servidor
    args = argparse.ArgumentParser(description="Servidor do TazzHelper")
    args.add_argument("--pasta", default=os.path.join(os.path.expanduser("~"), "TazzCode", "ia"),
                      help="pasta onde ficam a mini IA e o llama.cpp")
    args.add_argument("--porta", type=int, default=PORTA)
    a = args.parse_args()
    mini_ia = MiniIA(a.pasta)
    servidor = ThreadingHTTPServer(("127.0.0.1", a.porta), Atendente)
    print(f"TazzHelper IA {VERSAO} ligado na porta {a.porta} (mini IA: {'sim' if mini_ia.instalada() else 'não instalada'})", flush=True)
    try:
        servidor.serve_forever()
    finally:
        mini_ia.desligar()


if __name__ == "__main__":
    main()
