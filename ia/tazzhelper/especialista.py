"""
O CÉREBRO ESPECIALISTA do TazzHelper (feito por nós, em Python puro).

Ele não "inventa" resposta: ele usa o que está na cabeça pronta (conhecimento.py)
e analisa o seu código. Por isso quase nunca erra — mas só sabe o que foi ensinado.

Como ele acha o conceito certo pra uma pergunta? Com TF-IDF + semelhança de cosseno:
  1) quebra a pergunta em palavras (sem acento, minúsculas)
  2) dá um "peso" pra cada palavra (palavras raras valem mais que "o", "de", "que")
  3) compara com as perguntas de exemplo de cada conceito
  4) se for parecida o suficiente, responde
"""
import math
import re
import unicodedata

from . import conhecimento
from .analisador import explicar_codigo, dicas_do_codigo

PALAVRAS_VAZIAS = set("""o a os as um uma de do da dos das que e em no na nos nas pra para por com como
eu voce vc me meu minha isso esse essa aqui ai e é ser serve funciona fazer faz tem ter qual quais""".split())


def normalizar(texto):
    """'Variável É?' -> 'variavel e'"""
    sem_acento = unicodedata.normalize("NFD", texto.lower())
    sem_acento = "".join(c for c in sem_acento if unicodedata.category(c) != "Mn")
    return re.sub(r"[^a-z0-9_ ]+", " ", sem_acento)


def palavras(texto):
    return [p for p in normalizar(texto).split() if p not in PALAVRAS_VAZIAS and len(p) > 1]


class Especialista:
    LIMITE = 0.33   # quão parecida a pergunta precisa ser (0 a 1)

    def __init__(self):
        # Monta o "índice": cada pergunta de exemplo vira um vetor de pesos
        self.exemplos = []   # (vetor, indice_do_conceito)
        documentos = []
        for i, c in enumerate(conhecimento.CONCEITOS):
            for p in c["perguntas"]:
                documentos.append((palavras(p), i))
        total = len(documentos)
        contagem = {}
        for ps, _ in documentos:
            for p in set(ps):
                contagem[p] = contagem.get(p, 0) + 1
        self.idf = {p: math.log((1 + total) / (1 + n)) + 1 for p, n in contagem.items()}
        for ps, i in documentos:
            self.exemplos.append((self._vetor(ps), i))
        self.erros = [(re.compile(r, re.IGNORECASE), e) for r, e in conhecimento.ERROS]

    def _vetor(self, ps):
        v = {}
        for p in ps:
            v[p] = v.get(p, 0) + self.idf.get(p, 1.0)
        norma = math.sqrt(sum(x * x for x in v.values())) or 1
        return {p: x / norma for p, x in v.items()}

    def _parecido(self, pergunta):
        """Devolve (conceito, nota) mais parecido com a pergunta."""
        v = self._vetor(palavras(pergunta))
        melhor, nota = None, 0.0
        for ex, i in self.exemplos:
            s = sum(v.get(p, 0) * x for p, x in ex.items())
            if s > nota:
                melhor, nota = i, s
        return melhor, nota

    # ------------------------------------------------------------------

    def explicar_erros(self, console):
        ditas, saida = set(), []
        for padrao, explicacao in self.erros:
            m = padrao.search(console or "")
            if not m:
                continue
            texto = explicacao
            for g in range(1, (m.lastindex or 0) + 1):
                texto = texto.replace("$" + str(g), (m.group(g) or "").strip())
            chave = explicacao[:25]
            if chave in ditas:
                continue
            ditas.add(chave)
            saida.append("• " + texto)
            if len(saida) >= 4:
                break
        linha = re.search(r"(?:\.java|\.py|\.js|\.c|\.cpp)[:\"]\s*(?:line\s*)?(\d+)", console or "")
        if saida and linha:
            saida.append("Olhe a **linha " + linha.group(1) + "** do arquivo.")
        return "\n\n".join(saida)

    def responder(self, pergunta, ctx):
        """
        Devolve (resposta, certeza). certeza alta = o especialista sabe responder.
        ctx: {"arquivo", "linguagem", "codigo", "console", "problemas", "professor"}
        """
        p = normalizar(pergunta)
        console = ctx.get("console") or ""
        codigo = ctx.get("codigo") or ""

        # 1) Erro
        if re.search(r"\berr[oa]|deu ruim|nao funciona|nao roda|bug|quebrou|por que deu", p):
            r = self.explicar_erros(console)
            problemas = ctx.get("problemas") or []
            if problemas:
                r += ("\n\n" if r else "") + "No HTML eu vi:\n" + "\n".join("• " + x for x in problemas[:5])
            if r:
                return r, 0.95
            if "[Terminou com código 0" in console:
                return ("O último programa rodou **sem erro**! Se ele não fez o que você queria, me conte "
                        "o que você esperava que acontecesse e o que aconteceu."), 0.6

        # 2) Explicar o código
        if re.search(r"explica\w* (o )?(meu )?codigo|o que (o )?(meu )?codigo faz|parte por parte", p) and codigo.strip():
            return explicar_codigo(codigo, ctx.get("linguagem", "")), 0.9

        # 3) Dica
        if re.search(r"\bdica\b|melhorar|proximo passo", p) and codigo.strip():
            return dicas_do_codigo(codigo, ctx.get("linguagem", "")), 0.85

        # 4) Cumprimentos
        if re.fullmatch(r"\s*(oi|ola|eai|e ai|bom dia|boa tarde|boa noite|salve)\b.*", p) and len(p) < 25:
            return "Oi! Eu sou o **TazzHelper**. Pergunte sobre programação, ou rode o código e clique em **Explicar o erro**.", 0.9
        if re.search(r"\bobrigad|valeu|vlw", p) and len(p) < 30:
            return "De nada! Bora programar!", 0.9

        # 5) Conceitos da cabeça pronta
        i, nota = self._parecido(pergunta)
        if i is not None and nota >= self.LIMITE:
            return conhecimento.CONCEITOS[i]["resposta"], min(0.95, 0.5 + nota)

        return "", 0.0
