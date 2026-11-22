"""
A "CABEÇA PRONTA" do TazzHelper: tudo o que o especialista já sabe.

  CONCEITOS -> perguntas de exemplo + a resposta (o que é variável, laço, classe...)
  ERROS     -> um padrão (regex) que aparece no erro + a explicação

DESAFIO: ensine coisas novas pro TazzHelper! É só copiar um item e mudar.
Quanto mais "perguntas" de exemplo um conceito tiver, mais fácil ele é achado.
"""

CONCEITOS = [
    {
        "perguntas": ["o que é uma variável", "como criar variável", "pra que serve variável", "declarar variável"],
        "resposta": "Uma **variável** é uma *caixinha com nome* que guarda um valor pra usar depois.\n\n"
                    "Java (precisa dizer o tipo):\n```\nint idade = 12;\nString nome = \"João\";\n```\n"
                    "Python (não precisa do tipo):\n```\nidade = 12\nnome = \"João\"\n```\n"
                    "Dica: dê nomes que explicam o que tem dentro (`pontos`, `vidas`), não `x` e `y`.",
    },
    {
        "perguntas": ["quais são os tipos de dados", "diferença entre int e double", "o que é int", "o que é string", "o que é boolean", "tipos primitivos"],
        "resposta": "Os **tipos** dizem que tipo de valor a variável guarda:\n"
                    "- `int` → número inteiro (`10`, `-3`)\n- `double` → número com vírgula (`3.14`) — no código a vírgula é **ponto**!\n"
                    "- `boolean` → verdadeiro ou falso (`true`/`false`)\n- `char` → uma letra (`'A'`)\n- `String` → um texto (`\"Olá\"`)\n\n"
                    "No Python é igual, mas os nomes são `int`, `float`, `bool` e `str`, e você não precisa escrever o tipo.",
    },
    {
        "perguntas": ["como funciona o if", "o que é if else", "condição", "como fazer uma decisão", "else if", "elif"],
        "resposta": "O **if** (\"se\") faz o programa **decidir**: só roda o bloco se a condição for verdadeira.\n\n"
                    "Java:\n```\nif (idade >= 12) {\n    System.out.println(\"Pode jogar!\");\n} else {\n    System.out.println(\"Ainda não.\");\n}\n```\n"
                    "Python:\n```\nif idade >= 12:\n    print(\"Pode jogar!\")\nelse:\n    print(\"Ainda não.\")\n```\n"
                    "Comparações: `==` (igual), `!=` (diferente), `>`, `<`, `>=`, `<=`. Pra juntar: `&&`/`and` (E), `||`/`or` (OU).",
    },
    {
        "perguntas": ["como funciona o for", "o que é laço", "loop", "repetir várias vezes", "como repetir código", "for each"],
        "resposta": "Um **laço** (loop) repete um pedaço do código. O **for** é bom quando você sabe **quantas vezes**.\n\n"
                    "Java:\n```\nfor (int i = 0; i < 5; i++) {\n    System.out.println(i);\n}\n```\n"
                    "Python:\n```\nfor i in range(5):\n    print(i)\n```\n"
                    "Os dois mostram 0, 1, 2, 3, 4. O `i` é o contador: começa em 0 e vai somando 1.",
    },
    {
        "perguntas": ["como funciona o while", "o que é while", "repetir enquanto", "laço infinito", "loop infinito"],
        "resposta": "O **while** (\"enquanto\") repete **enquanto** a condição for verdadeira. Bom quando você **não sabe** quantas vezes.\n\n"
                    "```\nwhile (vidas > 0) {\n    jogar();\n}\n```\n"
                    "Cuidado com o **laço infinito**: se a condição nunca ficar falsa, o programa nunca para! "
                    "Garanta que algo lá dentro muda (tipo `vidas--`). Se travar, aperte **Parar** no TazzCode.",
    },
    {
        "perguntas": ["o que é função", "o que é método", "como criar função", "como criar método", "pra que serve função", "def", "return"],
        "resposta": "Uma **função** (no Java se chama **método**) é um pedaço de código com nome, que você pode usar várias vezes.\n\n"
                    "Java:\n```\nstatic int dobro(int n) {\n    return n * 2;\n}\n```\n"
                    "Python:\n```\ndef dobro(n):\n    return n * 2\n```\n"
                    "`n` é o **parâmetro** (o que entra) e o `return` devolve o resultado (o que sai). Usar: `dobro(5)` dá `10`.",
    },
    {
        "perguntas": ["o que é classe", "o que é objeto", "programação orientada a objetos", "poo", "como criar classe", "o que é new"],
        "resposta": "Uma **classe** é uma *forma de bolo*; um **objeto** é o *bolo* feito com ela.\n\n"
                    "```\nclass Jogador {\n    String nome;\n    int vidas = 3;\n\n    void perderVida() {\n        vidas--;\n    }\n}\n\n"
                    "Jogador j = new Jogador();   // o new cria o objeto\nj.nome = \"João\";\nj.perderVida();\n```\n"
                    "A classe tem **atributos** (o que o objeto TEM: nome, vidas) e **métodos** (o que ele FAZ: perderVida).",
    },
    {
        "perguntas": ["o que é static", "pra que serve static", "non-static", "método estático"],
        "resposta": "**static** quer dizer que a coisa é **da classe**, e não de cada objeto.\n\n"
                    "O `main` é `static`, então ele só enxerga direto outras coisas `static`. Por isso aparece o erro "
                    "\"non-static ... cannot be referenced from a static context\".\n\n"
                    "Duas saídas: coloque `static` no método, **ou** crie um objeto com `new` e chame pelo objeto.",
    },
    {
        "perguntas": ["o que é array", "o que é vetor", "o que é lista", "arraylist", "como guardar vários valores", "list python"],
        "resposta": "**Arrays e listas** guardam vários valores numa variável só, cada um numa **posição** (que começa no **0**!).\n\n"
                    "Java (array, tamanho fixo):\n```\nint[] notas = {8, 9, 10};\nSystem.out.println(notas[0]); // 8\n```\n"
                    "Java (ArrayList, cresce sozinha):\n```\nArrayList<String> jogos = new ArrayList<>();\njogos.add(\"Minecraft\");\n```\n"
                    "Python:\n```\njogos = [\"Minecraft\", \"Roblox\"]\njogos.append(\"BlockBricks\")\n```",
    },
    {
        "perguntas": ["o que é dicionário", "hashmap", "map", "chave e valor"],
        "resposta": "Um **dicionário** (no Java, **HashMap**) guarda pares **chave → valor**, tipo uma agenda.\n\n"
                    "Python:\n```\nidades = {\"João\": 12, \"Maria\": 13}\nprint(idades[\"João\"])\n```\n"
                    "Java:\n```\nHashMap<String, Integer> idades = new HashMap<>();\nidades.put(\"João\", 12);\n```",
    },
    {
        "perguntas": ["como ler do teclado", "scanner", "input", "como pedir um número pro usuário", "nextint", "nextline"],
        "resposta": "Pra **ler o que a pessoa digita**:\n\nJava:\n```\nScanner sc = new Scanner(System.in);\nString nome = sc.nextLine();\nint idade = sc.nextInt();\n```\n"
                    "(Precisa do `import java.util.Scanner;` lá em cima.)\n\nPython:\n```\nnome = input(\"Nome: \")\nidade = int(input(\"Idade: \"))\n```\n"
                    "No TazzCode, você digita na **caixinha de baixo do console** e aperta ENTER.\n\n"
                    "Pegadinha do Java: depois de `nextInt()`, o `nextLine()` seguinte vem vazio. Coloque um `sc.nextLine();` extra pra \"limpar\".",
    },
    {
        "perguntas": ["como mostrar na tela", "print", "println", "system out println", "console log", "imprimir"],
        "resposta": "Pra **mostrar algo na tela**:\n- Java: `System.out.println(\"Olá\");` (dica do TazzCode: digite `sout` e aperte ENTER)\n"
                    "- Python: `print(\"Olá\")`\n- JavaScript: `console.log(\"Olá\");`\n\n"
                    "Pra juntar texto com variável: Java `\"Idade: \" + idade`, Python `f\"Idade: {idade}\"`.",
    },
    {
        "perguntas": ["o que é string", "como juntar texto", "concatenar", "comparar string", "equals"],
        "resposta": "**String** é texto. Juntar com `+`: `\"Olá, \" + nome`.\n\n"
                    "Pegadinha do Java: pra **comparar** textos use `.equals`, não `==`!\n```\nif (nome.equals(\"João\")) { ... }\n```\n"
                    "Métodos úteis: `.length()`, `.toUpperCase()`, `.contains(\"x\")`, `.charAt(0)`.",
    },
    {
        "perguntas": ["o que é main", "public static void main", "por que precisa do main", "onde o programa começa"],
        "resposta": "O **main** é a **porta de entrada**: o programa Java começa a rodar por ele.\n\n"
                    "```\npublic static void main(String[] args) {\n    // começa aqui\n}\n```\n"
                    "- `public`: todo mundo pode chamar\n- `static`: não precisa de objeto\n- `void`: não devolve nada\n"
                    "- `String[] args`: textos que podem vir quando o programa abre\n\nAtalho no TazzCode: digite `main` e ENTER.",
    },
    {
        "perguntas": ["o que é import", "pra que serve import", "biblioteca", "pacote"],
        "resposta": "O **import** traz código pronto de outro lugar (uma **biblioteca**), pra você não precisar escrever tudo.\n\n"
                    "Java: `import java.util.Scanner;`\nPython: `import random` (e depois `random.randint(1, 6)`)\n\n"
                    "No Python, bibliotecas de fora se instalam com `python -m pip install nome` (no Terminal do TazzCode).",
    },
    {
        "perguntas": ["diferença entre compilar e interpretar", "o que é compilar", "o que é compilador", "javac"],
        "resposta": "**Compilar** é traduzir o código inteiro pra linguagem do computador **antes** de rodar (C, C++, Java, Rust).\n"
                    "**Interpretar** é ir traduzindo **enquanto** roda (Python, JavaScript).\n\n"
                    "Por isso no Java alguns erros aparecem **antes** do programa rodar (\"compilation failed\"): o compilador achou algo errado na escrita.",
    },
    {
        "perguntas": ["o que é erro de sintaxe", "syntax error", "erro de escrita", "por que meu código não roda"],
        "resposta": "**Erro de sintaxe** é erro de **escrita**: o computador não entendeu a \"gramática\" do código.\n\n"
                    "Os mais comuns:\n- faltou `;` no fim da linha (Java/C/JS)\n- faltou fechar `)`, `}` ou `\"`\n- escreveu o nome errado (maiúscula conta!)\n"
                    "- no Python: faltou `:` no fim do `if`/`for`/`def`, ou o recuo (espaços) está errado\n\n"
                    "O erro sempre diz a **linha**. Comece olhando ela (e a de cima!).",
    },
    {
        "perguntas": ["como achar um bug", "como depurar", "debug", "meu programa não faz o que eu quero", "resultado errado"],
        "resposta": "Caçar **bug** (depurar) é tipo ser detetive:\n1. Descubra **onde** dá errado: coloque `println`/`print` mostrando o valor das variáveis em vários pontos.\n"
                    "2. Compare o que **deveria** acontecer com o que **acontece**.\n3. Teste com valores pequenos e fáceis de conferir de cabeça.\n"
                    "4. Mude **uma coisa por vez** e rode de novo.\n\nE você pode clicar em **Explicar o erro** aqui no TazzHelper!",
    },
    {
        "perguntas": ["o que é recursão", "função que chama ela mesma", "recursividade"],
        "resposta": "**Recursão** é quando uma função **chama ela mesma**, com um problema menor, até chegar num caso simples (a \"parada\").\n\n"
                    "```\nstatic int fatorial(int n) {\n    if (n <= 1) return 1;        // parada\n    return n * fatorial(n - 1);  // chama ela mesma\n}\n```\n"
                    "Sem a parada, ela nunca acaba (StackOverflowError!).",
    },
    {
        "perguntas": ["o que é html", "como funciona html", "o que é tag", "tags html"],
        "resposta": "**HTML** é a linguagem que monta a **estrutura** de uma página: títulos, textos, botões, imagens.\n\n"
                    "Ele usa **tags**, que normalmente abrem e fecham:\n```\n<h1>Título</h1>\n<p>Um parágrafo.</p>\n<button>Clique</button>\n```\n"
                    "Algumas não fecham: `<br>`, `<img>`, `<input>`. O TazzCode sublinha as tags erradas (aba **Problemas**).",
    },
    {
        "perguntas": ["o que é css", "como mudar a cor", "como estilizar", "seletor css", "deixar a página bonita"],
        "resposta": "**CSS** é o que deixa a página **bonita**: cores, tamanhos, posições.\n\n"
                    "```\nh1 {\n    color: #2F7DF6;\n    font-size: 40px;\n}\n.botao {\n    background: black;\n}\n```\n"
                    "`h1` pega todas as tags h1; `.botao` pega quem tem `class=\"botao\"`; `#menu` pega quem tem `id=\"menu\"`.\n"
                    "Ligue o CSS no HTML com `<link rel=\"stylesheet\" href=\"style.css\">`.",
    },
    {
        "perguntas": ["o que é javascript", "como fazer o botão funcionar", "dom", "getelementbyid", "evento de clique"],
        "resposta": "**JavaScript** dá **vida** à página: faz o botão funcionar, muda textos, cria jogos.\n\n"
                    "```\nconst botao = document.getElementById(\"botao\");\nbotao.addEventListener(\"click\", () => {\n    alert(\"Clicou!\");\n});\n```\n"
                    "O `getElementById` pega um elemento do HTML pelo `id`.",
    },
    {
        "perguntas": ["o que é sql", "o que é banco de dados", "select", "como fazer consulta", "tabela"],
        "resposta": "**SQL** é a linguagem pra conversar com **bancos de dados** (que guardam informação em **tabelas**, tipo planilhas).\n\n"
                    "```\nCREATE TABLE alunos (id INT, nome VARCHAR(50));\nINSERT INTO alunos VALUES (1, 'João');\nSELECT * FROM alunos WHERE id = 1;\n```\n"
                    "No TazzCode, F5 num `.sql` abre um **banco simulado** pra testar à vontade.",
    },
    {
        "perguntas": ["o que é git", "versionamento", "github", "commit"],
        "resposta": "**Git** guarda o **histórico** do seu código: cada \"foto\" é um **commit**. Se der ruim, dá pra voltar no tempo!\n"
                    "O **GitHub** é um site pra guardar esses projetos na internet e trabalhar junto com outras pessoas.\n\n"
                    "Comandos básicos: `git init`, `git add .`, `git commit -m \"mensagem\"`.",
    },
    {
        "perguntas": ["o que é null", "nullpointerexception", "null pointer", "objeto vazio"],
        "resposta": "**null** quer dizer \"**nada aqui**\": a variável ainda não aponta pra nenhum objeto.\n\n"
                    "Se você tentar usar (`texto.length()`) quando `texto` é `null`, dá **NullPointerException**.\n"
                    "Garanta que a variável recebeu um valor (`= new ...` ou `= \"algo\"`) antes de usar.",
    },
    {
        "perguntas": ["o que é exceção", "try catch", "tratar erro", "exception"],
        "resposta": "Uma **exceção** é um erro que acontece **enquanto** o programa roda. Com `try/catch` você \"segura\" o erro:\n\n"
                    "```\ntry {\n    int n = Integer.parseInt(texto);\n} catch (NumberFormatException e) {\n    System.out.println(\"Isso não é número!\");\n}\n```\n"
                    "No Python é `try:` / `except ValueError:`.",
    },
    {
        "perguntas": ["como gerar número aleatório", "random", "sortear número", "dado aleatório"],
        "resposta": "**Números aleatórios** (sortear):\n\nJava:\n```\nRandom r = new Random();\nint dado = r.nextInt(6) + 1;   // 1 a 6\n```\n"
                    "Python:\n```\nimport random\ndado = random.randint(1, 6)\n```",
    },
    {
        "perguntas": ["o que é swing", "como criar janela em java", "jframe", "jbutton", "interface gráfica"],
        "resposta": "**Swing** é a biblioteca do Java pra fazer **janelas** (o próprio TazzCode é feito com ela!).\n\n"
                    "```\nJFrame janela = new JFrame(\"Minha janela\");\nJButton botao = new JButton(\"Clique\");\njanela.add(botao);\n"
                    "janela.setSize(300, 200);\njanela.setVisible(true);\n```\n"
                    "Tem um modelo pronto: Novo projeto → **Java Swing (janela)**.",
    },
    {
        "perguntas": ["como fazer um jogo", "fazer jogo", "criar game", "loop do jogo"],
        "resposta": "Todo jogo tem um **loop do jogo** que roda muitas vezes por segundo:\n1. ler o teclado/mouse\n2. atualizar (mover, colidir, pontuar)\n3. desenhar a tela\n\n"
                    "Pra começar, tem modelos prontos no Novo projeto: **Pygame** (Python), **Phaser** e **p5.js** (web), **C raylib**.",
    },
    {
        "perguntas": ["o que é indentação", "recuo", "espaços no começo da linha", "indentationerror"],
        "resposta": "**Indentação** (recuo) são os espaços no começo da linha que mostram o que está **dentro** de quê.\n\n"
                    "No **Python ela é obrigatória**: o que está dentro do `if`/`for`/`def` precisa de 4 espaços a mais (sempre iguais).\n"
                    "No Java ela é só pra ficar legível (quem manda são as `{ }`), mas é muito importante pra entender o código!",
    },
    {
        "perguntas": ["o que é o tazzcode", "quem criou o tazzcode", "o que você é", "quem é você", "quem é o tazzhelper"],
        "resposta": "Eu sou o **TazzHelper**, a IA do **TazzCode** — o editor de código criado pelo **João**! "
                    "Eu rodo aqui no seu computador, sem internet: tenho um **cérebro especialista** (que sabe erros e conceitos de programação) "
                    "e uma **mini IA** (a Gemma 3 270M, do Google) pra conversar sobre o resto.",
    },
    {
        "perguntas": ["como usar o tazzcode", "atalhos do tazzcode", "como rodar o código", "f5"],
        "resposta": "Atalhos do **TazzCode**:\n- **F5** roda (Ctrl+F5 roda só o arquivo)\n- **Ctrl+S** salva, **Ctrl+N** arquivo novo\n"
                    "- **Ctrl+Espaço** sugestões, `sout`/`main`/`fori` viram código\n- **Ctrl+/** comenta a linha, **Ctrl+D** duplica\n"
                    "- **Alt+Enter** corrige o erro do HTML\n- Menu **Plugins** instala linguagens sem mexer no Windows",
    },
]

# (padrão regex, explicação) — $1, $2... são pedaços capturados do erro
ERROS = [
    (r"cannot find symbol[\s\S]*?symbol:\s+(?:variable|method|class)\s+(\w+)",
     "**O Java não conhece `$1`.** Pode ser: nome escrito errado (maiúscula/minúscula conta!), variável criada dentro de outro bloco `{ }`, ou faltou um `import`."),
    (r"';' expected", "**Faltou um ponto e vírgula `;`** no fim de uma linha."),
    (r"class (\w+) is public, should be declared in a file named (\S+)",
     "**O nome da classe tem que ser igual ao nome do arquivo.** A classe é `$1`, então o arquivo tem que se chamar `$2`."),
    (r"incompatible types: (.+?) cannot be converted to (\S+)",
     "**Tipos diferentes:** você tentou guardar um `$1` onde só cabe `$2`. Pra converter texto em número: `Integer.parseInt(...)`."),
    (r"missing return statement", "**Faltou o `return`.** O método promete devolver um valor, mas em algum caminho termina sem `return`."),
    (r"reached end of file while parsing", "**Faltou fechar uma chave `}`.** Conte as `{` e as `}`: tem que ter o mesmo tanto."),
    (r"unclosed string literal", "**Um texto ficou sem fechar as aspas.** Todo `\"` que abre precisa de outro fechando, na mesma linha."),
    (r"non-static (?:variable|method) (\w+).*cannot be referenced from a static context",
     "**`$1` não é `static`, mas o `main` é.** Coloque `static` em `$1`, ou crie um objeto com `new` e use por ele."),
    (r"NullPointerException", "**NullPointerException:** você usou um objeto vazio (`null`). Confira se a variável recebeu um valor antes."),
    (r"ArrayIndexOutOfBoundsException.*?Index (-?\d+) out of bounds for length (\d+)",
     "**Posição fora da lista:** pediu a posição $1, mas a lista tem $2 itens (vai de 0 até $2 menos 1)."),
    (r"InputMismatchException", "**O Scanner esperava um número e veio texto** (ou o contrário)."),
    (r"NumberFormatException: For input string: \"(.*?)\"", "**\"$1\" não é um número**, então não deu pra converter."),
    (r"ArithmeticException: / by zero", "**Divisão por zero!** Confira o divisor antes de dividir."),
    (r"IndentationError", "**Erro de recuo (espaços).** No Python, o que está dentro de `if`/`for`/`def` precisa de 4 espaços a mais, todos iguais."),
    (r"NameError: name '(\w+)' is not defined", "**O Python não conhece `$1`.** Escreveu errado, ou usou antes de criar (`$1 = ...`)?"),
    (r"ModuleNotFoundError: No module named '([\w.]+)'", "**Falta instalar `$1`.** No Terminal: `python -m pip install $1`"),
    (r"TypeError: can only concatenate str \(not \"(\w+)\"\) to str", "**Não dá pra juntar texto com $1 usando `+`.** Use `str(...)` ou f-string: `f\"Idade: {idade}\"`."),
    (r"ZeroDivisionError", "**Divisão por zero!**"),
    (r"IndexError: list index out of range", "**Posição fora da lista.** Lembre que começa no 0."),
    (r"KeyError: (.+)", "**A chave $1 não existe no dicionário.** Use `.get(...)` ou confira se ela foi colocada antes."),
    (r"ValueError: invalid literal for int\(\) with base 10: '(.*?)'", "**\"$1\" não é um número inteiro**, então o `int()` não conseguiu converter."),
    (r"SyntaxError: (.+)", "**Erro de escrita (sintaxe):** $1. Confira parênteses, aspas e os `:` no fim de `if`/`for`/`def`."),
    (r"ReferenceError: (\w+) is not defined", "**O JavaScript não conhece `$1`.** Criou com `let`/`const` antes de usar?"),
    (r"Cannot read propert(?:y|ies) of (undefined|null)", "**Você usou algo que está `$1`.** Ex.: `pessoa.nome` quando `pessoa` ainda não existe."),
    (r"Cannot find module '(.+?)'", "**Falta instalar o pacote `$1`.** No Terminal: `npm install $1`"),
    (r"undefined reference to `?(\w+)", "**O compilador não achou a função `$1`.** O nome está certo? Se for de biblioteca, faltou ligar ela."),
    (r"não está instalado|is not recognized as an internal or external command|command not found",
     "**O programa dessa linguagem não está instalado.** Use o menu **Plugins** (roda sem instalar no Windows) ou o **Tutorial de instalação**."),
    (r"ERRO: a tabela \"(\w+)\" não existe", "**A tabela `$1` não existe ainda.** Crie com `CREATE TABLE $1 (...)` antes."),
]
