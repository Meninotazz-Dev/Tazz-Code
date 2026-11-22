# TazzCode — versão 0.7

Um editor de código feito em Java (Swing): projetos, abas, cores no código, autocompletar e um console pra rodar o código.

## Como abrir

1. Extraia o zip (botão direito → **Extrair tudo**).
2. Dê dois cliques em **Rodar.bat**. Se o editor não aparecer, use o **Rodar-VerErros.bat**: ele deixa a janela preta aberta mostrando o erro.

Pra gerar um `.jar` (dá pra abrir com dois cliques), use o **Empacotar.bat**. O arquivo vai ficar em `dist\TazzCode.jar`.

## Novidades da 0.7: A IA PRÓPRIA DO TAZZHELPER

Agora o TazzHelper tem **cabeça própria**, feita em **Python**, rodando **no seu PC**:
sem internet, sem chave, de graça. Ela é **híbrida** (2 cérebros):

| Cérebro | O que faz | Tamanho |
|---|---|---|
| **Especialista** (já vem pronto) | explica erros do console, conceitos (variável, for, classe, recursão, HTML, SQL...), explica o seu código linha por linha, dá dicas | menos de 1 MB |
| **Mini IA** (opcional) | conversa sobre qualquer coisa. É a Gemma 3 270M + o llama.cpp | ~280 MB |

O especialista responde primeiro. Se ele não souber, a mini IA responde (ela é pequena: **pode errar!**).

- Escolha o cérebro em **TazzHelper > Configurar**: IA do TazzCode / Claude pela internet / simples.
- Precisa de **Python** (o do PC ou o **plugin do Python**).
- A mini IA se instala pelo botão **Instalar a mini IA** (no Configurar). Fica em `C:\ProgramData\TazzCode\ia`.
- O código da IA fica na pasta `ia` do TazzCode. Dá pra mexer: as respostas do especialista estão em
  `ia/tazzhelper/conhecimento.py` — **ensine coisas novas pra ele!**

### Pastas organizadas (igual programa de verdade)

| Pasta | O que tem | Por quê |
|---|---|---|
| `C:\Program Files\TazzCode` | o programa (TazzCode.exe, o Java, o código da IA) | é onde o Windows espera os programas; só o instalador mexe |
| `C:\Users\SeuNome\AppData\Roaming\TazzCode` | configurações (config.properties) e erro.log | cada usuário do PC tem as suas |
| `C:\Users\SeuNome\Documents\TazzCode` | seus **projetos** e rascunhos | fácil de achar e de fazer backup |
| `C:\ProgramData\TazzCode` | plugins, mini IA, compilados, temp | coisas grandes, e **sem acento nem espaço** (o GCC precisa disso) |

Quem tinha a versão antiga (tudo em `C:\Users\SeuNome\TazzCode`): na primeira vez que abrir, o TazzCode
**muda tudo de lugar sozinho** e avisa. Nada é apagado.

### TazzCode.exe e instalador

Rode o **Empacotar.bat**. Ele cria:
1. `dist\app\TazzCode\TazzCode.exe` — com o **Java dentro** (quem usar não precisa instalar Java!)
2. `dist\Instalar-TazzCode-0.7.exe` — o instalador que coloca tudo no Program Files, cria atalho no
   menu Iniciar e aparece em "Adicionar ou remover programas". Precisa do **Inno Setup** (grátis):
   `winget install JRSoftware.InnoSetup`

O seu ícone: coloque `recursos\icone.png` (quadrado, 256x256) antes de empacotar.

### Visual novo e CONFIGURAÇÕES (0.7)

- **Visual moderno:** botões arredondados com ícones, barra de cima organizada em grupos,
  abas com um risquinho colorido, barra de baixo discreta, caixas de escolher e de diálogo no tema.
- **Configurações** (Arquivo > Configurações, **Ctrl+vírgula**, ou a engrenagem ⚙):
  - **Aparência:** 4 temas (**Meia-noite**, **Grafite**, **Oceano** e **Claro**), a **fonte da interface**
    (ex.: Sans-Serif) e o tamanho, a **fonte do código** e o tamanho, com prévia na hora.
  - **Editor:** tamanho do TAB (2, 4 ou 8), fechar parênteses sozinho, sugestões, números das linhas,
    destacar a linha do cursor, quebrar linhas compridas e **salvar sozinho** a cada 30 segundos.
  - **Console:** tamanho da letra e limpar ou não antes de rodar.
  - **TazzHelper:** mostrar ao abrir e modo professor.
  - **Pastas:** onde cada coisa fica, com botão pra abrir.
- Trocar o tema ou a fonte da interface **reinicia** o TazzCode sozinho (ele pede pra salvar e reabre os arquivos).

### Arrumados (0.7)

- **Travamento de minutos:** quando um programa escrevia MUITO no console (ex.: `while (true)` com `println`),
  o TazzCode travava. Agora o console junta a saída e guarda só o final (200 mil letras), sem travar.
- **Vigia de travamentos:** se a janela ficar parada mais de 5 segundos, o TazzCode anota o motivo em
  `travamentos.log` (Arquivo > Abrir pasta das configurações). Se travar, mande esse arquivo pro Claude!
- **"public class Main" virava o atalho "main":** o autocompletar agora não sugere nada logo depois de
  `class`, `interface`, `enum`, `def`, `function`... e com letra maiúscula só sugere o que bate certinho.

### LINUX (12 distribuições)

| Pacote | Distribuições | Instalar |
|---|---|---|
| `tazzcode_0.7_amd64.deb` | Ubuntu, Debian, Linux Mint, Kali, Pop!_OS, Zorin OS, Winux, elementary OS | `sudo apt install ./tazzcode_0.7_amd64.deb` |
| `tazzcode-0.7-1.x86_64.rpm` | Fedora, openSUSE | `sudo dnf install ./...rpm` (openSUSE: `sudo zypper install ./...rpm`) |
| `tazzcode-0.7-1-x86_64.pkg.tar.zst` | Arch Linux, Manjaro, EndeavourOS | `sudo pacman -U tazzcode-0.7-1-x86_64.pkg.tar.zst` |
| `tazzcode-0.7-linux-x64.tar.gz` | qualquer outro | `tar xzf ...tar.gz && ./tazzcode-0.7/instalar.sh` (sem senha, só pro seu usuário) |

- Todos trazem o **Java dentro**. Precisa de um Linux de 2024 pra frente (glibc 2.38+: Ubuntu 24.04, Debian 13, Mint 22, Fedora 40...).
- **Criar os pacotes:** `bash empacotar-linux.sh` (precisa do JDK; pro .rpm, do `rpm`; pro Arch, do `bsdtar` e do `zstd`).
  Se faltar a ferramenta de um formato, ele pula só aquele.
- **Rodar pelo código:** `bash rodar.sh`.
- **Pastas no Linux:** configurações em `~/.config/tazzcode`, projetos em `~/Documentos/TazzCode`,
  mini IA e compilados em `~/.local/share/tazzcode`.
- **Sem plugins no Linux:** o **Tutorial de instalação** descobre qual é o seu Linux e mostra o comando certo
  (`apt`, `dnf`, `zypper` ou `pacman`), e abre o Terminal pra você.
- O Python no Linux se chama `python3`: o TazzCode entende isso sozinho.

**Arrumado (0.7):** o GCC (C/C++) dava `ld.exe: cannot find C:/Users/João...` quando o nome do usuário
tem acento ou espaço. Agora os **plugins**, os **compilados** e os arquivos temporários ficam numa pasta
"segura": `C:\ProgramData\TazzCode` (os plugins que você já tinha são movidos sozinhos, sem baixar de novo).

**Antivírus (0.7):** o Avast/Windows Defender desconfia de todo .exe novinho (ele nunca viu antes).
Agora o console avisa que isso é normal, e **Ajuda > Por que o antivírus desconfia?** explica como
colocar a pasta dos compilados como exceção (peça pra um adulto).

Como funciona por dentro: o TazzCode liga `python ia/servidor.py`, que fica escutando na porta 7878
do seu PC. A mini IA roda num outro programinha (`llama-server`) na porta 7879.

## Novidades da 0.6: 49 PLUGINS (versão anterior)

Agora são **49 plugins** (eram 13). Das 84 linguagens que rodam pelo console, **61 já têm plugin** (ou já vêm no Windows).

**Novos:** Lua, Ruby, Rust, Perl, R, GNU Octave (MATLAB), Racket (e Scheme), SWI-Prolog, Erlang, Elixir, Gleam, Free Pascal, Nim, Haxe, V, Scala, Groovy, Maven (pros modelos Spring Boot/JavaFX), D, Git Bash (bash, sh, awk), NASM (Assembly), AutoHotkey, AutoIt, Ballerina, FreeBASIC, Babashka (Clojure), CMake, ISPC, Lean 4, Processing, Raku, Solidity, Terraform, GHDL (VHDL), Wasmtime (WebAssembly) e Arduino CLI.

Os plugins agora vêm de 4 jeitos diferentes (o TazzCode cuida de tudo sozinho):
- **.zip portátil:** baixa e extrai (a maioria)
- **instalador silencioso:** instala sem perguntar nada, mas DENTRO da pasta do TazzCode (Ruby, R, Racket, SWI-Prolog, Erlang, Free Pascal). Pode aparecer uma janela do Windows pedindo permissão.
- **programa único:** um .exe sozinho (Solidity)
- **instalador oficial por script:** Rust (rustup) e .NET

Plugins que precisam de outro plugin instalam ele junto: Elixir e Gleam precisam do Erlang; Kotlin, Groovy e Maven precisam do JDK; Nim e NASM precisam do GCC.

**Ainda sem plugin (e por quê):**
- **Não existem pro Windows:** Fish, Zsh, Hack e Nix. Use o WSL.
- **Só têm instalador grande ou complicado:** Haskell, OCaml, Swift, Ada, COBOL, Crystal, Odin e Common Lisp. Elas aparecem no tutorial.
- **Pouco usadas e sem versão portátil:** Forth, Icon, Io, J, Janet, Smalltalk, Tcl, Vala e Verilog.
- **Grandes demais pra plugin:** LaTeX (MiKTeX tem mais de 1 GB) e Docker (precisa do Docker Desktop).

## Novidades da 0.5: PLUGINS e TUTORIAIS (versão anterior)

### Plugins: linguagens portáteis
Menu **Plugins → Gerenciar plugins**. O TazzCode baixa a linguagem e guarda em `C:\Users\SeuNome\TazzCode\plugins`.
Ela **roda sem instalar nada no Windows**, e dá pra apagar quando quiser.

| Plugin | Faz rodar | Tamanho |
|---|---|---|
| Python (+ pip) | Python | ~30 MB |
| Node.js (node, npm, npx) | JavaScript, TypeScript, CoffeeScript, EJS, modelos Node/React | ~30 MB |
| Java (JDK 21) | Java, JShell | ~190 MB |
| Go | Go | ~80 MB |
| Deno / Bun | JavaScript, TypeScript | ~40 MB cada |
| PHP | PHP | ~30 MB |
| C / C++ / Fortran (GCC) | C, C++, Fortran, Makefile | ~250 MB |
| Zig / Julia / Kotlin / Dart | cada uma a sua | 80–200 MB |
| .NET SDK | C#, F# | ~250 MB |

- Cada plugin **descobre a versão mais nova sozinho** na hora de baixar (não fica velho).
- **O que está instalado no PC tem prioridade.** O plugin é o plano B.
- Se você apertar F5 e faltar o programa, o TazzCode **pergunta se quer instalar o plugin** (ou ver o tutorial).

### Tutorial de instalação no PC
Menu **Plugins → Tutorial de instalação**. Ele tem um passo a passo pra cada linguagem:
1. **Plugin** (o jeito mais fácil)
2. **winget** (o instalador que já vem no Windows 10/11): um botão roda o comando pra você
3. **Pelo site**, explicando o "Add to PATH"
4. Botão **Testar agora**, que mostra se a linguagem foi achada (ex.: `go version`)

### Arrumado
- `npm`, `npx` e outros programas `.cmd` agora rodam certinho no Windows (antes o Java não achava eles).
- Se o Windows tiver aquele `python` falso que só abre a Microsoft Store, o TazzCode usa o plugin no lugar.

## Novidades da 0.4 (versão anterior)

### ◆ TazzHelper (o "Claude" do TazzCode)
O painel da direita (botão **◆ TazzHelper** ou Ctrl+Shift+H). Ele vê o arquivo aberto, o console e os problemas.
- **Sem chave (offline):** explica uns 40 erros comuns (Java, Python, JavaScript, C, SQL, "programa não instalado"...). Clique em **Explicar o erro** ou no botão **◆ Explicar com o TazzHelper** do console.
- **Com chave da API da Anthropic:** vira uma IA de verdade (o Claude). Dá pra perguntar qualquer coisa, pedir pra explicar o código ou pedir uma dica. A resposta aparece aos pouquinhos, enquanto ele "escreve".
- **Modo professor** (ligado no começo): dá dicas e explica, mas não entrega o exercício pronto. Dá pra desligar em *Configurar*.
- A chave é **paga** (cada pergunta custa alguns centavos): um adulto precisa criar em console.anthropic.com e colar em *Configurar*. Ela fica salva no `config.properties` da pasta do TazzCode: não mande esse arquivo pra ninguém.

### Verificador de HTML
- `<DOCTYPE html>` não ganha mais um `</doctype>` automático.
- Erros ficam com uma **ondinha** vermelha (erro) ou amarela (aviso). Passe o mouse em cima pra ler, ou aperte **Alt+Enter** pra corrigir.
- A aba **Problemas** (do lado do Console) lista tudo, com **Corrigir esse** e **Corrigir tudo**.
- Ele acha: DOCTYPE sem `!`, `</doctype>`, `</br>`, tag sem fechar, fechamento sem abertura, tags trocadas (`<b><i></b></i>`), tag que não existe (`<dvi>` → "quis dizer `<div>`?"), aspas sem fechar, `id` repetido, e dá dicas (`alt` no `<img>`, `<title>`, `charset`).

### Tudo do OneCompiler (os 166 itens de onecompiler.com/languages)
- **Linguagens novas:** J, Icon, ISPC, JShell e EJS (agora são 114 no total).
- **"Rodar com"** (a caixinha do lado do Rodar): JavaScript/TypeScript com **Node.js, Deno ou Bun**; **Python 3 ou 2**; **Octave ou MATLAB**; **Bash ou sh**; Java **arquivo único ou a pasta toda** (pra programas com várias classes!).
- **16 bancos simulados:** no `.sql` dá pra escolher **MySQL, MariaDB, PostgreSQL, SQLite, SQL Server, Oracle, IBM Db2, DuckDB, QuestDB, Firebird ou ClickHouse**. Também tem arquivos próprios: **MongoDB** (`.mongo`), **Redis** (`.redis`), **Cassandra** (`.cql`), **SurrealDB** (`.surql`), **PostgreSQL** (`.pgsql`) e **MySQL** (`.mysql`). O MongoDB e o Redis têm motores próprios: documentos JSON e chave-valor.
- **83 modelos de projeto** no *Novo projeto* (tem busca: digite "react", "flask"...):
  - **Python:** Flask, FastAPI, Django, Streamlit, Gradio, Bottle, Tornado, Tkinter, Turtle, Pygame, Matplotlib, Seaborn
  - **Node.js:** Express, Fastify, Koa, Hono, EJS, NestJS
  - **Java:** Swing, JavaFX, Spring Boot, Javalin
  - **Go:** Gin, Echo, Fiber
  - **Outras:** Rust Axum, Ruby on Rails, PHP Web, Laravel, Symfony, C raylib, C# Avalonia
  - **Web:** Bootstrap, Tailwind, daisyUI, Bulma, Foundation, Materialize, UIkit, Semantic UI, Skeleton, Milligram, PaperCSS, Sass, jQuery, Alpine.js, HTMX, Backbone, Vue, Vuetify, Preact, Lit, Chart.js, D3, ECharts, Three.js, PixiJS, Phaser, p5.js, Anime.js, GSAP, Motion, Leaflet, Mermaid, Reveal.js, Marked, KaTeX, Ionic
  - **React:** React, MUI, Chakra UI, Mantine, React Bootstrap, Radix UI, Headless UI, PrimeReact, Recharts (funcionam sem instalar nada!)
  - **Projetos grandes:** Next.js, Angular, Svelte, React com Vite, shadcn/ui
- Quando o modelo precisa de biblioteca (ex.: `pip install flask`), o TazzCode **instala sozinho** quando cria o projeto.
- Cada projeto de modelo tem um arquivo `.tazzcode` com o **comando do F5** (ex.: `python app.py`). Pra mudar: *Executar → Configurar o comando do projeto*. **Ctrl+F5** roda só o arquivo aberto.
- Servidores (Flask, Express, Spring...) abrem o navegador sozinhos depois de ligar.
- As páginas web abrem por um **servidorzinho do próprio TazzCode** (`http://localhost:5500`), porque React e `import` não funcionam abrindo o arquivo direto.

## Novidades da 0.3 (versão anterior)

- **103 linguagens!** As 50 da sua lista + 50 extras + HTML, CSS e .bat. Cada uma tem cores, palavras-chave, funções, atalhos no autocompletar, um modelo "Olá, mundo!" e um jeito de rodar.
- **Banco de dados simulado**: arquivos `.sql`, `.pls` (PL/SQL) e `.tsql` (T-SQL) abrem uma janela com um banco de mentirinha.
- **Brainfuck** roda direto aqui dentro (o TazzCode tem um interpretador próprio).
- **Compilar + rodar** em passos: C, C++, Rust, Pascal, Fortran, COBOL... compilam primeiro e depois rodam. Os programas compilados ficam em `C:\Users\SeuNome\TazzCode\compilados`.
- **Se faltar algum programa instalado** (ex.: o Go), o console mostra o site pra baixar.
- **DOCTYPE arrumado**: o `!` só cria a página inteira se o arquivo ainda não tem `<html>`. Depois de `<` ele completa só o nome da tag (`<di` → `<div></div>`).
- **Novo arquivo e Novo projeto** agora têm todas as linguagens na lista.

## As linguagens

**1 a 10:** Python, JavaScript, TypeScript, Java, C#, C++, C, Go, Rust, SQL
**11 a 25:** PHP, Swift, Kotlin, R, Dart, Ruby, MATLAB (roda com o Octave), Shell/Bash, PowerShell, Assembly (NASM), Scala, Lua, Perl, VB.NET, Delphi/Pascal
**26 a 50:** Julia, Zig, Mojo, Elixir, Haskell, Objective-C (`.mm`), Groovy, VBA, Fortran, COBOL, ABAP, Ada, Solidity, F#, Clojure, Erlang, Gleam, Verilog, VHDL, SAS, Apex, PL/SQL, T-SQL, Nim, Scratch
**51 a 100 (extras):** Common Lisp, Scheme, Racket, OCaml, Prolog (`.pro`), Smalltalk, Tcl, AWK, Crystal, D, V (`.vsh`), Odin, Hack, CoffeeScript, Elm, PureScript, Haxe, Nix, GDScript, AutoHotkey, AutoIt, Raku, Io, Forth, Zsh, Fish, Makefile, CMake, Dockerfile, YAML, TOML, JSON, XML/SVG, Markdown, LaTeX, GLSL, HLSL, WebAssembly, GraphQL, Terraform, Arduino, Processing, Logo, BASIC, VBScript, Brainfuck, Lean, Janet, Ballerina, Vala

**Pra rodar**, cada linguagem precisa do programa dela instalado (o TazzCode só chama ele). Algumas já funcionam sem instalar nada: **Java** (você já tem o JDK), **PowerShell**, **.bat**, **VBScript**, **HTML**, **SQL/PL-SQL/T-SQL** (banco simulado) e **Brainfuck**.
Algumas só dá pra **editar** aqui, porque rodam dentro de outro programa: VBA (Excel), ABAP (SAP), SAS, Apex (Salesforce), GDScript (Godot), Elm, PureScript, shaders, YAML, JSON... O F5 explica onde rodar cada uma.

> Repare que algumas usam uma extensão diferente porque duas linguagens usam a mesma: `.v` é Verilog (o V usa `.vsh`), `.m` é MATLAB (o Objective-C usa `.mm`), `.pl` é Perl (o Prolog usa `.pro`), `.bas` é BASIC (o VBA usa `.vba`).

## O banco de dados simulado

Aperte F5 num `.sql` e abre uma janela com:
- **Tabelas** na esquerda (clique numa pra ver o que tem dentro)
- **Resultados** de cada SELECT, cada um numa aba
- **Mensagens** embaixo, e uma caixinha **SQL>** pra digitar comandos na hora

Ele entende: `CREATE TABLE`, `INSERT`, `SELECT` (com `WHERE`, `JOIN`, `LEFT JOIN`, `GROUP BY`, `HAVING`, `ORDER BY`, `LIMIT`, `DISTINCT`, `COUNT/SUM/AVG/MIN/MAX`, `CASE`, subconsultas), `UPDATE`, `DELETE`, `DROP`, `ALTER TABLE ADD/DROP COLUMN`, `TRUNCATE`, `SHOW TABLES`, `DESCRIBE`.
- **T-SQL:** `TOP`, `PRINT`, `DECLARE @x`, `SET @x`, `IF/ELSE`, `WHILE`, `BEGIN/END`, `GO`
- **PL/SQL:** `DECLARE`, `BEGIN/END`, `:=`, `IF/ELSIF/ELSE`, `FOR i IN 1..10 LOOP`, `WHILE ... LOOP`, `DBMS_OUTPUT.PUT_LINE`, `FROM dual`, `||`

É de mentirinha: fica só na memória. Cada vez que você roda o arquivo, o banco começa do zero.

## Novidades da 0.2 (versão anterior)

- **Tela inicial** igual à do IntelliJ: lista de projetos, *Novo projeto* (Java, Python, Site ou vazio), *Abrir pasta*, *Abrir arquivo*
- **Árvore do projeto** na esquerda. Dois cliques abrem o arquivo; o botão direito tem novo arquivo, nova pasta, renomear, excluir (vai pra Lixeira) e mostrar no Explorador. Alt+1 mostra e esconde a árvore.
- **Novo arquivo com tipo** (Ctrl+N): Classe Java, Classe Java com main, Python, HTML, CSS, JavaScript... e o arquivo já vem com o modelo escrito (ex.: `public class Nome { }`)
- **Cores no código**: Java, Python, JavaScript, HTML, CSS e .bat
- **Autocompletar** enquanto digita (ENTER ou TAB aceita, ESC fecha, Ctrl+Espaço abre na hora):
  - Java: `sout`, `souf`, `main`/`psvm`, `fori`, `if`, `ifelse`, `while`, `switch`, `try`, `class`, `Scanner`...
  - Python: `print`, `input`, `def`, `for`, `if`...
  - HTML: `!` (página completa), `div`, `p`, `h1`, `button`, `img`, `a`...
  - CSS: `color`, `background-color`, `font-size`, `margin`...
  - Também sugere nomes que você já usou no próprio arquivo
- **Fecha sozinho** `( )`, `[ ]`, `{ }`, `" "` e as tags do HTML (`<div>` vira `<div></div>`)
- **ENTER entre `{ }`** abre o bloco já com o recuo certo
- **Abas com ×** pra fechar, e a aba escolhida aparece em azul (antes o nome sumia)
- **Terminal** (botão `>_ Terminal` ou Alt+F12): abre o `cmd` na pasta do projeto, dentro do console
- **Salvar tudo** (Ctrl+Alt+S)
- Menus, abas e barras de rolagem no tema escuro

## Atalhos

| Atalho | O que faz |
|---|---|
| Ctrl+N / Ctrl+Shift+N | Novo arquivo (com tipo) / Novo sem título |
| Ctrl+O | Abrir arquivo |
| Ctrl+S / Ctrl+Shift+S / Ctrl+Alt+S | Salvar / Salvar como / Salvar tudo |
| Ctrl+W | Fechar aba |
| F5 / Shift+F5 | Rodar / Parar |
| Alt+F12 | Terminal |
| Ctrl+Z / Ctrl+Y | Desfazer / Refazer |
| Ctrl+/ | Comentar/descomentar a linha |
| Ctrl+D | Duplicar a linha |
| Shift+TAB | Tirar o recuo da linha |
| Ctrl+Espaço | Mostrar sugestões |
| Alt+1 / Ctrl+J | Mostrar/esconder a árvore / o console |
| Ctrl+= / Ctrl+- | Aumentar / diminuir a fonte |

## Linguagens que ele roda (F5)

| Arquivo | Como roda | Precisa ter instalado |
|---|---|---|
| (veja a lista completa acima: são 103 linguagens) | | |
| `.java` | `java Arquivo.java` | JDK |
| `.py` | `python arquivo.py` | Python |
| `.js` | `node arquivo.js` | Node.js |
| `.html` | abre no navegador | nada |
| `.bat` / `.cmd` | `cmd /c` | nada |
| `.ps1` | PowerShell | nada |

Por enquanto, um `.java` precisa ter o programa todo num arquivo só. Rodar projetos Java com várias classes fica pra uma próxima versão.

## O "mini banco" (igual ao BlockBricks)

Fica em `C:\Users\SeuNome\TazzCode\`:

- `config.properties`: fonte, tamanho da janela, arquivos e projetos recentes
- `projetos\`: onde o **Novo projeto** cria as pastas
- `erro.log`: só aparece se der algum erro

## Ícone

Desenhe o seu ícone, salve como **`recursos/icone.png`** (quadrado, tipo 256×256) e rode o `Rodar.bat` de novo.

## Desafios pra você

- ⭐ Mude as cores do código no `Tema.java` (procure `COD_PALAVRA`)
- ⭐ Crie um atalho novo no `CatalogoPrincipais.java` (procure `.atalho("sout"` e copie)
- ⭐⭐ Cadastre uma linguagem nova no `CatalogoExtras.java` (copie uma parecida!)
- ⭐⭐ Faça o Ctrl+/ funcionar no CSS (comentário do CSS é `/* ... */`)
- ⭐⭐⭐ Crie um menu "Buscar" (Ctrl+F) que procura uma palavra no texto
