"""
A MINI IA: o cérebro que conversa sobre qualquer coisa.

Ela é a Gemma 3 270M (do Google, grátis pra usar), uma IA pequenininha que roda no seu
computador, sem internet. Quem faz ela "pensar" é o llama.cpp (o programa llama-server).

O caminho de uma pergunta:
  servidor.py  ->  mini_ia.py  ->  llama-server (porta 7879)  ->  modelo .gguf
                                    (a resposta volta aos pedacinhos)

Ela é bem mais fraca que as IAs grandes: às vezes erra ou inventa. Por isso o
especialista responde primeiro, e ela só entra quando ele não sabe.
"""
import json
import os
import subprocess
import sys
import time
import urllib.request

PORTA = 7879
MODELO = "gemma-3-270m-it-Q4_K_M.gguf"


class MiniIA:
    def __init__(self, pasta):
        self.pasta = pasta
        self.processo = None

    # ---------- onde estão os arquivos ----------

    def modelo(self):
        return os.path.join(self.pasta, "modelo", MODELO)

    def servidor_llama(self):
        nome = "llama-server.exe" if os.name == "nt" else "llama-server"
        for raiz, _, arquivos in os.walk(os.path.join(self.pasta, "llama")):
            if nome in arquivos:
                return os.path.join(raiz, nome)
        return None

    def instalada(self):
        return os.path.isfile(self.modelo()) and self.servidor_llama() is not None

    # ---------- ligar / desligar ----------

    def _respondendo(self):
        try:
            with urllib.request.urlopen(f"http://127.0.0.1:{PORTA}/health", timeout=2) as r:
                return r.status == 200
        except Exception:
            return False

    def ligar(self):
        if self._respondendo():
            return True
        if not self.instalada():
            return False
        cmd = [self.servidor_llama(), "-m", self.modelo(), "--host", "127.0.0.1", "--port", str(PORTA),
               "-c", "4096", "--threads", str(max(1, (os.cpu_count() or 2) // 2))]
        flags = 0x08000000 if os.name == "nt" else 0   # CREATE_NO_WINDOW: sem janela preta
        env = dict(os.environ)
        if os.name != "nt":   # no Linux, as bibliotecas (.so) ficam do lado do llama-server
            pasta = os.path.dirname(self.servidor_llama())
            env["LD_LIBRARY_PATH"] = pasta + os.pathsep + env.get("LD_LIBRARY_PATH", "")
        self.processo = subprocess.Popen(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, creationflags=flags, env=env)
        for _ in range(120):   # espera até 60 s o modelo carregar
            if self._respondendo():
                return True
            if self.processo.poll() is not None:
                return False
            time.sleep(0.5)
        return False

    def desligar(self):
        if self.processo and self.processo.poll() is None:
            self.processo.terminate()

    # ---------- conversar ----------

    def conversar(self, mensagens):
        """mensagens = [{"role": "system"/"user"/"assistant", "content": "..."}]. Vai devolvendo pedacinhos."""
        corpo = json.dumps({"messages": mensagens, "stream": True, "temperature": 0.4, "max_tokens": 700}).encode("utf-8")
        req = urllib.request.Request(f"http://127.0.0.1:{PORTA}/v1/chat/completions", data=corpo,
                                     headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req, timeout=300) as r:
            for linha in r:
                linha = linha.decode("utf-8").strip()
                if not linha.startswith("data:"):
                    continue
                dado = linha[5:].strip()
                if dado == "[DONE]":
                    break
                try:
                    pedaco = json.loads(dado)["choices"][0]["delta"].get("content")
                except (KeyError, IndexError, ValueError):
                    continue
                if pedaco:
                    yield pedaco


def instrucoes(ctx):
    """O 'system prompt' da mini IA (curtinho, porque ela é pequena)."""
    professor = ctx.get("professor", True)
    texto = ("Você é o TazzHelper, assistente de programação do editor TazzCode. Responda em português do Brasil, "
             "de forma curta, simples e gentil, para um estudante jovem. ")
    if professor:
        texto += "Não entregue exercícios resolvidos: dê dicas e explique a ideia. "
    if ctx.get("codigo"):
        texto += f"\n\nO aluno está com o arquivo {ctx.get('arquivo') or ''} ({ctx.get('linguagem') or ''}) aberto:\n{ctx['codigo'][:2500]}"
    if ctx.get("console"):
        texto += f"\n\nConsole:\n{ctx['console'][-1000:]}"
    return texto


if __name__ == "__main__":
    # Teste rápido: python -m tazzhelper.mini_ia <pasta>
    ia = MiniIA(sys.argv[1] if len(sys.argv) > 1 else ".")
    print("instalada:", ia.instalada())
