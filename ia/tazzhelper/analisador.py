"""
O ANALISADOR DE CÓDIGO: lê o seu código linha por linha e explica o que cada parte faz.
Ele reconhece "jeitos" de escrever (padrões), tipo:
    for (int i = 0; ...)   -> "um laço que repete"
    if (...)               -> "uma decisão"
    System.out.println(...) -> "mostra na tela"
"""
import re

# (linguagens, padrão, explicação) — {1}, {2}... = pedaços capturados
REGRAS = [
    # ----- Java / C# / C / C++ / JavaScript -----
    ("java", r"^\s*import\s+([\w.]+)", "traz a ferramenta `{1}` pra usar no código"),
    ("java", r"^\s*package\s+([\w.]+)", "diz que esse arquivo é do pacote `{1}`"),
    ("java", r"^\s*(?:public\s+)?(?:abstract\s+|final\s+)?class\s+(\w+)", "começa a **classe** `{1}` (a \"forma\" do programa)"),
    ("java", r"public\s+static\s+void\s+main", "o **main**: é por aqui que o programa começa"),
    ("java c js", r"^\s*(?:public|private|protected)?\s*(?:static\s+)?(\w+(?:<.*?>)?(?:\[\])?)\s+(\w+)\s*\(([^)]*)\)\s*\{", "cria o método `{2}` (devolve `{1}`)"),
    ("java", r"Scanner\s+(\w+)\s*=\s*new\s+Scanner", "cria o Scanner `{1}` pra ler o teclado"),
    ("java", r"\b(\w+)\.next(Int|Line|Double)?\(\)", "lê o que a pessoa digitar"),
    ("java", r"System\.out\.print(?:ln|f)?\((.*)\)", "mostra na tela: `{1}`"),
    ("java c js", r"^\s*for\s*\(\s*(?:int|let|var)?\s*(\w+)\s*=\s*([^;]+);\s*([^;]+);", "um **laço for**: o `{1}` começa em `{2}` e repete enquanto `{3}`"),
    ("java js", r"^\s*for\s*\(\s*(?:\w+\s+|const\s+|let\s+)?(\w+)\s*(?::|of|in)\s*(\w+)", "um **for-each**: passa por cada item de `{2}` (chamando de `{1}`)"),
    ("java c js", r"^\s*while\s*\((.*)\)", "um **laço while**: repete enquanto `{1}`"),
    ("java c js", r"^\s*(?:\}\s*)?else\s+if\s*\((.*)\)", "senão, **se** `{1}`..."),
    ("java c js", r"^\s*if\s*\((.*)\)", "uma **decisão**: só entra se `{1}`"),
    ("java c js", r"^\s*(?:\}\s*)?else\b", "**senão** (quando o if não aconteceu)"),
    ("java c js", r"^\s*return\s+(.+?);", "devolve `{1}`"),
    ("java c js", r"^\s*(?:final\s+)?(int|double|float|long|boolean|char|String|var|let|const)\s+(\w+)\s*=\s*(.+?);", "cria a variável `{2}` ({1}) valendo `{3}`"),
    ("java c js", r"^\s*(int|double|float|long|boolean|char|String)\s+(\w+)\s*;", "cria a variável `{2}` ({1}), ainda sem valor"),
    ("java c js", r"^\s*(\w+)\s*(\+\+|--)\s*;", "`{2}` em `{1}` (soma/tira 1)"),
    ("java c js", r"^\s*(\w+)\s*([+\-*/]?=)\s*(.+?);", "muda o valor de `{1}` (`{2} {3}`)"),
    ("java", r"new\s+(\w+)\s*\(", "cria um objeto `{1}` com o **new**"),
    ("java js", r"^\s*try\s*\{", "**tenta** fazer o que vem dentro..."),
    ("java js", r"catch\s*\((\w+)", "...e se der o erro `{1}`, faz isso"),
    ("c", r"^\s*#include\s*[<\"](.+?)[>\"]", "inclui a biblioteca `{1}`"),
    ("c", r"printf\((.*)\)", "mostra na tela: `{1}`"),
    ("c", r"scanf\(", "lê o que a pessoa digitar"),
    ("js", r"console\.log\((.*)\)", "mostra no console: `{1}`"),
    ("js", r"function\s+(\w+)\s*\(([^)]*)\)", "cria a função `{1}`"),
    ("js", r"(?:const|let)\s+(\w+)\s*=\s*\(?([^)=]*)\)?\s*=>", "cria a função (seta) `{1}`"),
    ("js", r"getElementById\(['\"](.+?)['\"]\)", "pega o elemento do HTML com id `{1}`"),
    ("js", r"addEventListener\(['\"](\w+)['\"]", "faz algo quando acontecer o evento `{1}`"),
    # ----- Python -----
    ("py", r"^\s*(?:from\s+(\w+)\s+)?import\s+([\w., ]+)", "traz a biblioteca `{1}{2}`"),
    ("py", r"^\s*def\s+(\w+)\s*\(([^)]*)\)", "cria a função `{1}` (recebe: `{2}`)"),
    ("py", r"^\s*class\s+(\w+)", "cria a **classe** `{1}`"),
    ("py", r"^\s*for\s+(\w+)\s+in\s+range\((.+)\):", "um **laço for**: o `{1}` vai passando por range({2})"),
    ("py", r"^\s*for\s+(\w+)\s+in\s+(.+):", "um **for**: passa por cada item de `{2}`"),
    ("py", r"^\s*while\s+(.+):", "um **laço while**: repete enquanto `{1}`"),
    ("py", r"^\s*elif\s+(.+):", "senão, **se** `{1}`..."),
    ("py", r"^\s*if\s+(.+):", "uma **decisão**: só entra se `{1}`"),
    ("py", r"^\s*else\s*:", "**senão**"),
    ("py", r"^\s*return\s+(.+)", "devolve `{1}`"),
    ("py", r"(\w+)\s*=\s*(?:int|float)?\(?input\((.*?)\)", "pergunta `{2}` e guarda a resposta em `{1}`"),
    ("py", r"^\s*print\((.*)\)", "mostra na tela: `{1}`"),
    ("py", r"^\s*(\w+)\s*([+\-*/]?=)\s*(.+)", "a variável `{1}` recebe `{3}`"),
    ("py", r"^\s*try\s*:", "**tenta** fazer o que vem dentro..."),
    ("py", r"^\s*except\s*(\w*)", "...e se der erro {1}, faz isso"),
    # ----- HTML -----
    ("html", r"<!DOCTYPE", "avisa que é uma página HTML moderna"),
    ("html", r"<title>(.*?)</title>", "o nome da aba do navegador: \"{1}\""),
    ("html", r"<link[^>]+href=\"(.+?\.css)\"", "liga o CSS `{1}` (as cores e estilos)"),
    ("html", r"<script[^>]+src=\"(.+?)\"", "liga o JavaScript `{1}`"),
    ("html", r"<h([1-6])[^>]*>(.*?)</h\1>", "um título (tamanho {1}): \"{2}\""),
    ("html", r"<p[^>]*>(.*?)</p>", "um parágrafo: \"{1}\""),
    ("html", r"<button[^>]*>(.*?)</button>", "um botão escrito \"{1}\""),
    ("html", r"<img[^>]+src=\"(.+?)\"", "mostra a imagem `{1}`"),
    ("html", r"<a[^>]+href=\"(.+?)\"", "um link pra `{1}`"),
    ("html", r"<(div|section|header|footer|nav|main)\b", "uma caixa/seção (`<{1}>`) pra organizar a página"),
]

FAMILIA = {"Java": "java", "C#": "java", "Kotlin": "java", "Groovy": "java", "C": "c", "C++": "c",
           "JavaScript": "js", "TypeScript": "js", "Python": "py", "Mojo": "py", "HTML": "html", "EJS (Embedded JavaScript)": "html"}


def _familia(linguagem):
    return FAMILIA.get(linguagem, "java")


def explicar_codigo(codigo, linguagem):
    fam = _familia(linguagem)
    regras = [(re.compile(r), e) for langs, r, e in REGRAS if fam in langs.split()]
    saida = ["Aqui vai o seu código (" + (linguagem or "?") + "), parte por parte:\n"]
    contadas = 0
    for n, linha in enumerate(codigo.split("\n"), start=1):
        if not linha.strip() or linha.strip() in ("{", "}", "};"):
            continue
        if re.match(r"^\s*(//|#|/\*|\*|<!--)", linha) and fam != "c":
            saida.append(f"• **linha {n}:** um comentário (o computador ignora, é recado pra quem lê)")
            contadas += 1
            continue
        for padrao, explicacao in regras:
            m = padrao.search(linha)
            if m:
                texto = explicacao
                for g in range(1, (m.lastindex or 0) + 1):
                    texto = texto.replace("{" + str(g) + "}", (m.group(g) or "").strip()[:60])
                texto = re.sub(r"\{\d\}", "", texto)
                saida.append(f"• **linha {n}:** {texto}")
                contadas += 1
                break
        if contadas >= 40:
            saida.append("• ... (o código é grande, expliquei as primeiras partes)")
            break
    if contadas == 0:
        return "Não reconheci partes desse código pra explicar. Me pergunte sobre uma linha específica!"
    return "\n".join(saida)


def dicas_do_codigo(codigo, linguagem):
    fam = _familia(linguagem)
    dicas = []
    linhas = codigo.split("\n")
    if not any(re.match(r"^\s*(//|#|/\*|<!--)", l) for l in linhas) and len(linhas) > 8:
        dicas.append("Coloque **comentários** explicando as partes principais (no futuro você vai agradecer!).")
    nomes_curtos = set(re.findall(r"\b(?:int|double|String|float|let|var|const)\s+([a-z])\s*=", codigo))
    if fam == "py":
        nomes_curtos |= set(re.findall(r"^\s*([a-z])\s*=", codigo, re.M))
    nomes_curtos -= {"i", "j", "k"}
    if nomes_curtos:
        dicas.append("Os nomes `" + "`, `".join(sorted(nomes_curtos)) + "` não dizem o que guardam. Que tal nomes como `pontos`, `idade`, `total`?")
    if fam == "java" and "Scanner" in codigo and ".close()" not in codigo:
        dicas.append("Você abriu um `Scanner`: quando terminar de ler, feche com `sc.close();`.")
    if fam == "java" and re.search(r"==\s*\"", codigo):
        dicas.append("Achei um `==` comparando texto. No Java, texto se compara com `.equals(...)`.")
    numeros = set(re.findall(r"[^\w.](\d{2,})[^\w.]", codigo)) - {"10", "100"}
    if len(numeros) >= 3:
        dicas.append("Tem vários números \"soltos\" no código (" + ", ".join(sorted(numeros)[:4]) + "). Guarde em variáveis com nome, tipo `final int VIDAS_INICIAIS = 3;`.")
    maior = max((len(l) for l in linhas), default=0)
    if maior > 120:
        dicas.append("Tem linha com mais de 120 letras. Quebrar em partes menores deixa mais fácil de ler.")
    if fam == "html" and "<meta charset" not in codigo.lower():
        dicas.append("Coloque `<meta charset=\"UTF-8\">` no `<head>` pros acentos aparecerem certinho.")
    if not dicas:
        dicas.append("Seu código está organizado! Próximo desafio: tente quebrar em **funções/métodos** menores, cada uma fazendo uma coisa só.")
    return "Algumas dicas (sem fazer por você):\n\n" + "\n".join("• " + d for d in dicas[:4])
