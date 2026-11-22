package tazzcode;

import static tazzcode.Catalogo.nova;

/**
 * Mais 50 linguagens (51 a 100), além de HTML, CSS e .bat (que o TazzCode já tinha).
 */
class CatalogoExtras {

    private static final String EXTRA = "51 a 100 - Extras";
    private static final String WEB = "Web e Windows";

    static void cadastrar() {

        // ===== as que o TazzCode já tinha =====================================

        nova(WEB, "HTML", "html", "htm")
            .estilo(Linguagem.Estilo.HTML).modo(Linguagem.Modo.NAVEGADOR)
            .modelo("<!DOCTYPE html>\n<html lang=\"pt-br\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>{nome}</title>\n"
                    + "    <link rel=\"stylesheet\" href=\"style.css\">\n</head>\n<body>\n    <h1>Olá, mundo!</h1>\n\n"
                    + "    <script src=\"script.js\"></script>\n</body>\n</html>\n")
            .precisa("Nada: abre no navegador.");

        nova(WEB, "CSS", "css")
            .estilo(Linguagem.Estilo.CSS).modo(Linguagem.Modo.SO_EDITAR)
            .modelo("body {\n    background-color: #1B2233;\n    color: white;\n    font-family: sans-serif;\n}\n")
            .precisa("O CSS não roda sozinho: ligue ele num HTML com <link rel=\"stylesheet\" href=\"style.css\"> e rode o HTML.");

        nova(WEB, "Batch (Windows)", "bat", "cmd")
            .semCaixa().linha("rem", "::").aspas("\"").variaveis("%")
            .palavras("echo set if else goto call exit for in do not exist defined pause cd start del mkdir rmdir copy "
                    + "move cls off setlocal endlocal errorlevel equ neq lss gtr")
            .modelo("@echo off\necho Ola, mundo!\npause\n")
            .rodar("cmd /c {arquivo}")
            .precisa("Já vem no Windows!")
            .atalho("echo", "mostra", "echo $|")
            .atalho("if", "se...", "if $| (\n    \n)");

        // ===== 51 a 100 =========================================================

        // 51. COMMON LISP
        nova(EXTRA, "Common Lisp", "lisp", "lsp", "cl")
            .linha(";").bloco("#|", "|#").aspas("\"")
            .palavras("defun defvar defparameter defmacro defclass lambda let let* if when unless cond case loop do "
                    + "dolist dotimes progn return setf setq quote and or not nil t")
            .funcoes("format print princ write-line read-line car cdr cons list append length mapcar reduce + - * /")
            .letras("-*!?<>=/+")
            .modelo("(format t \"Olá, mundo!~%\")\n")
            .rodar("sbcl --script {arquivo}")
            .precisa("SBCL: https://www.sbcl.org/platform-table.html")
            .atalho("format", "mostra", "(format t \"$|~%\")")
            .atalho("defun", "cria função", "(defun $| ()\n  )");

        // 52. SCHEME
        nova(EXTRA, "Scheme", "scm", "ss")
            .linha(";").bloco("#|", "|#").aspas("\"")
            .palavras("define lambda let let* letrec if cond case and or begin do else quote set!")
            .funcoes("display newline write read car cdr cons list map for-each apply length append")
            .letras("-*!?<>=/+")
            .modelo("(display \"Olá, mundo!\")\n(newline)\n")
            .rodar("scheme --script {arquivo}")
            .precisa("Chez Scheme: https://cisco.github.io/ChezScheme (ou use o Racket)")
            .atalho("display", "mostra", "(display $|)\n(newline)")
            .atalho("define", "cria", "(define ($|)\n  )");

        // 53. RACKET
        nova(EXTRA, "Racket", "rkt")
            .linha(";").bloco("#|", "|#").aspas("\"")
            .palavras("define lambda let let* if cond case and or begin when unless for for/list require provide struct else")
            .funcoes("displayln display printf println read-line map filter foldl first rest cons list length")
            .letras("-*!?<>=/+#")
            .modelo("#lang racket\n\n(displayln \"Olá, mundo!\")\n")
            .rodar("racket {arquivo}")
            .precisa("Racket: https://download.racket-lang.org")
            .atalho("displayln", "mostra", "(displayln $|)")
            .atalho("define", "cria", "(define ($|)\n  )");

        // 54. OCAML
        nova(EXTRA, "OCaml", "ml", "mli")
            .bloco("(*", "*)").aspas("\"'")
            .palavras("and as assert begin class constraint do done downto else end exception external false for fun "
                    + "function functor if in include inherit lazy let match method module mutable new object of open "
                    + "or private rec sig struct then to true try type val virtual when while with")
            .funcoes("print_endline print_string print_int Printf.printf read_line List.map List.iter string_of_int int_of_string")
            .tipos("int float string bool char list array unit option")
            .letras(".")
            .modelo("let () = print_endline \"Olá, mundo!\"\n")
            .rodar("ocaml {arquivo}")
            .precisa("OCaml: https://ocaml.org/install")
            .atalho("print", "mostra", "print_endline $|")
            .atalho("let", "valor", "let $| = ");

        // 55. PROLOG
        nova(EXTRA, "Prolog", "pro", "prolog")
            .linha("%").bloco("/*", "*/").aspas("\"'")
            .palavras("is not true fail halt module use_module dynamic")
            .funcoes("write writeln nl format read member append length findall assert retract")
            .modelo("pai(joao, pedro).\npai(pedro, ana).\n\navo(X, Z) :- pai(X, Y), pai(Y, Z).\n\n"
                    + "main :- writeln('Olá, mundo!'), avo(joao, Quem), format('Neta: ~w~n', [Quem]).\n")
            .rodar("swipl -q -s {arquivo} -g main -t halt")
            .precisa("SWI-Prolog: https://www.swi-prolog.org/download/stable (o programa começa no \"main\")")
            .atalho("writeln", "mostra", "writeln($|)");

        // 56. SMALLTALK
        nova(EXTRA, "Smalltalk", "st")
            .bloco("\"", "\"").aspas("'")
            .palavras("self super nil true false thisContext ifTrue: ifFalse: whileTrue: timesRepeat: do: to:")
            .funcoes("displayNl printNl show: cr new")
            .tipos("Transcript Object String Integer Array OrderedCollection")
            .letras(":")
            .modelo("Transcript showCr: 'Olá, mundo!'.\n")
            .rodar("gst {arquivo}")
            .precisa("GNU Smalltalk (gst), mais fácil pelo WSL ou MSYS2. Ou o Pharo: https://pharo.org")
            .atalho("show", "mostra", "Transcript showCr: '$|'.");

        // 57. TCL
        nova(EXTRA, "Tcl", "tcl")
            .linha("#").aspas("\"").variaveis("$")
            .palavras("set proc if else elseif for foreach while return break continue switch expr incr global upvar")
            .funcoes("puts gets string list lappend lindex llength split join format")
            .modelo("puts \"Olá, mundo!\"\n")
            .rodar("tclsh {arquivo}")
            .precisa("Tcl: https://www.magicsplat.com/tcl-installer")
            .atalho("puts", "mostra", "puts \"$|\"")
            .atalho("proc", "cria função", "proc $| {} {\n    \n}");

        // 58. AWK
        nova(EXTRA, "AWK", "awk")
            .linha("#").aspas("\"").variaveis("$")
            .palavras("BEGIN END if else while for do break continue next exit function return in getline")
            .funcoes("print printf length substr index split sub gsub match tolower toupper sprintf")
            .tipos("NR NF FS OFS RS")
            .modelo("BEGIN {\n    print \"Olá, mundo!\"\n}\n")
            .rodar("awk -f {arquivo}")
            .precisa("AWK vem com o Git for Windows (Git Bash) ou pelo MSYS2.")
            .atalho("begin", "bloco BEGIN", "BEGIN {\n    $|\n}");

        // 59. CRYSTAL
        nova(EXTRA, "Crystal", "cr")
            .linha("#").aspas("\"'").variaveis("@")
            .palavras("abstract alias annotation as begin break case class def do else elsif end ensure enum extend "
                    + "false for fun if include lib macro module next nil of out private protected require rescue "
                    + "return self struct super then true type unless until when while with yield")
            .funcoes("puts print p gets each map select times")
            .tipos("Int32 Float64 String Bool Array Hash")
            .modelo("puts \"Olá, mundo!\"\n")
            .rodar("crystal run {arquivo}")
            .precisa("Crystal: https://crystal-lang.org/install")
            .atalho("puts", "mostra", "puts $|")
            .atalho("def", "cria método", "def $|\n  \nend");

        // 60. D
        nova(EXTRA, "D", "d")
            .linha("//").bloco("/*", "*/").bloco("/+", "+/").aspas("\"'`")
            .palavras("abstract alias align asm assert auto body break case cast catch class const continue default "
                    + "delegate delete do else enum export extern false final finally for foreach function goto if "
                    + "immutable import in interface is lazy module new null out override package pragma private "
                    + "protected public pure ref return scope shared static struct super switch template this throw "
                    + "true try typeof union unittest void while with")
            .funcoes("writeln write readln writefln to")
            .tipos("int long double float string char bool size_t")
            .modelo("import std.stdio;\n\nvoid main() {\n    writeln(\"Olá, mundo!\");\n}\n")
            .rodar("rdmd {arquivo}")
            .precisa("D (DMD): https://dlang.org/download.html")
            .atalho("writeln", "mostra", "writeln($|);");

        // 61. V
        nova(EXTRA, "V", "vsh", "vv")
            .linha("//").bloco("/*", "*/").aspas("\"'`")
            .palavras("as asm assert atomic break const continue defer else enum false fn for go goto if import in "
                    + "interface is lock match module mut none or pub return rlock select shared sizeof struct true "
                    + "type typeof union unsafe")
            .funcoes("println print eprintln input len")
            .tipos("int i64 f64 string bool u8 rune")
            .modelo("println('Olá, mundo!')\n")
            .rodar("v run {arquivo}")
            .precisa("V: https://vlang.io (no TazzCode use .vsh, porque .v é do Verilog)")
            .atalho("println", "mostra", "println($|)")
            .atalho("fn", "cria função", "fn $|() {\n    \n}");

        // 62. ODIN
        nova(EXTRA, "Odin", "odin")
            .linha("//").bloco("/*", "*/").aspas("\"'`")
            .palavras("package import proc struct enum union if else for in when switch case return defer break "
                    + "continue fallthrough using cast transmute distinct dynamic map true false nil")
            .funcoes("println printf print len append")
            .tipos("int f64 string bool rune u8 i32")
            .modelo("package main\n\nimport \"core:fmt\"\n\nmain :: proc() {\n    fmt.println(\"Olá, mundo!\")\n}\n")
            .rodar("odin run {arquivo} -file")
            .precisa("Odin: https://odin-lang.org/docs/install")
            .atalho("println", "mostra", "fmt.println($|)");

        // 63. HACK
        nova(EXTRA, "Hack", "hack")
            .linha("//", "#").bloco("/*", "*/").aspas("\"'").variaveis("$")
            .palavras("async await class function if else foreach for while return new public private protected static "
                    + "use namespace enum shape type newtype vec dict keyset true false null")
            .funcoes("echo print")
            .tipos("int string bool float void mixed")
            .modelo("<<__EntryPoint>>\nfunction main(): void {\n  echo \"Olá, mundo!\\n\";\n}\n")
            .rodar("hhvm {arquivo}")
            .precisa("HHVM (só roda no Linux, use o WSL): https://docs.hhvm.com/hhvm/installation/introduction")
            .atalho("echo", "mostra", "echo $|;");

        // 64. COFFEESCRIPT
        nova(EXTRA, "CoffeeScript", "coffee")
            .linha("#").bloco("###", "###").aspas("\"'")
            .palavras("if else unless then for in of while until loop break continue return class extends new this "
                    + "true false null undefined yes no on off and or not is isnt try catch finally switch when")
            .funcoes("console.log alert")
            .letras(".")
            .modelo("console.log \"Olá, mundo!\"\n")
            .rodar("npx -y -p coffeescript coffee {arquivo}")
            .precisa("Node.js (o CoffeeScript vem pelo npx): https://nodejs.org")
            .atalho("log", "mostra", "console.log $|");

        // 65. ELM
        nova(EXTRA, "Elm", "elm")
            .linha("--").bloco("{-", "-}").aspas("\"'")
            .palavras("module exposing import as type alias case of if then else let in port")
            .funcoes("text div button h1 onClick")
            .tipos("Int Float String Bool Html Msg Model List Maybe")
            .modelo("module Main exposing (main)\n\nimport Html exposing (text)\n\nmain =\n    text \"Olá, mundo!\"\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("Elm roda no navegador: instale (https://guide.elm-lang.org/install) e use \"elm reactor\" no terminal.")
            .atalho("case", "escolha", "case $| of\n    _ ->\n        ");

        // 66. PURESCRIPT
        nova(EXTRA, "PureScript", "purs")
            .linha("--").bloco("{-", "-}").aspas("\"'")
            .palavras("module where import data type class instance let in if then else case of do forall")
            .funcoes("log show map pure")
            .tipos("Effect Unit Int String Number Boolean Array")
            .modelo("module Main where\n\nimport Prelude\nimport Effect.Console (log)\n\nmain = log \"Olá, mundo!\"\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("PureScript precisa de um projeto: \"npm create purescript\" no terminal (https://www.purescript.org).");

        // 67. HAXE
        nova(EXTRA, "Haxe", "hx")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("abstract break case cast catch class continue default do dynamic else enum extends extern false "
                    + "final for function if implements import in inline interface macro new null override package "
                    + "private public return static super switch this throw true try typedef untyped using var while")
            .funcoes("trace Std.string Std.parseInt")
            .tipos("Int Float String Bool Array Map Dynamic Void")
            .modelo("class {nome} {\n    static function main() {\n        trace(\"Olá, mundo!\");\n    }\n}\n")
            .rodar("haxe --main {nome} --interp")
            .precisa("Haxe: https://haxe.org/download (o nome da classe = nome do arquivo)")
            .atalho("trace", "mostra", "trace($|);");

        // 68. NIX
        nova(EXTRA, "Nix", "nix")
            .linha("#").bloco("/*", "*/").aspas("\"")
            .palavras("let in with rec inherit if then else assert import true false null or")
            .funcoes("builtins.map builtins.length builtins.toString")
            .letras(".")
            .modelo("let\n  mensagem = \"Olá, mundo!\";\nin\n  mensagem\n")
            .rodar("nix-instantiate --eval {arquivo}")
            .precisa("Nix (Linux/WSL): https://nixos.org/download");

        // 69. GDSCRIPT
        nova(EXTRA, "GDScript (Godot)", "gd")
            .linha("#").aspas("\"'")
            .palavras("extends class_name func var const signal enum if elif else for while match return pass break "
                    + "continue self true false null and or not in await static")
            .funcoes("print push_error get_node queue_free emit_signal _ready _process _physics_process")
            .tipos("int float String bool Vector2 Vector3 Node Node2D Sprite2D")
            .modelo("extends Node\n\nfunc _ready():\n    print(\"Olá, mundo!\")\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("GDScript roda dentro do Godot (motor de jogos grátis): https://godotengine.org/download")
            .atalho("func", "cria função", "func $|():\n    ")
            .atalho("ready", "_ready", "func _ready():\n    $|");

        // 70. AUTOHOTKEY
        nova(EXTRA, "AutoHotkey", "ahk")
            .semCaixa().linha(";").bloco("/*", "*/").aspas("\"")
            .palavras("if else loop while for in return break continue global local static class extends true false")
            .funcoes("MsgBox Send Sleep Run WinActivate InputBox ExitApp Hotkey")
            .modelo("MsgBox \"Olá, mundo!\"\n")
            .rodar("AutoHotkey64 {arquivo}")
            .precisa("AutoHotkey v2: https://www.autohotkey.com (adicione a pasta dele no PATH)")
            .atalho("msgbox", "mostra", "MsgBox \"$|\"");

        // 71. AUTOIT
        nova(EXTRA, "AutoIt", "au3")
            .semCaixa().linha(";").aspas("\"'").variaveis("$")
            .palavras("If Then Else ElseIf EndIf For To Next While WEnd Do Until Func EndFunc Return Local Global Dim "
                    + "Switch Case EndSwitch Exit True False")
            .funcoes("MsgBox ConsoleWrite Sleep Run Send WinActivate InputBox")
            .modelo("ConsoleWrite(\"Olá, mundo!\" & @CRLF)\n")
            .rodar("AutoIt3 {arquivo}")
            .precisa("AutoIt: https://www.autoitscript.com/site/autoit/downloads")
            .atalho("msgbox", "mostra", "MsgBox(0, \"TazzCode\", \"$|\")");

        // 72. RAKU
        nova(EXTRA, "Raku", "raku", "rakumod", "p6")
            .linha("#").aspas("\"'").variaveis("$@%")
            .palavras("my our sub method class role if elsif else unless for while until loop given when return "
                    + "use module has is does True False")
            .funcoes("say print put prompt get lines chars elems push pop map grep sort")
            .modelo("say \"Olá, mundo!\";\n")
            .rodar("raku {arquivo}")
            .precisa("Rakudo: https://rakudo.org/downloads")
            .atalho("say", "mostra", "say $|;");

        // 73. IO
        nova(EXTRA, "Io", "io")
            .linha("//", "#").bloco("/*", "*/").aspas("\"")
            .palavras("method block if then else for while loop return break continue self true false nil")
            .funcoes("println print clone setSlot")
            .tipos("Object")
            .modelo("\"Olá, mundo!\" println\n")
            .rodar("io {arquivo}")
            .precisa("Io: https://iolanguage.org");

        // 74. FORTH
        nova(EXTRA, "Forth", "fth", "4th", "forth")
            .semCaixa().linha("\\").bloco("( ", ")").aspas("\"")
            .palavras(": ; if else then do loop begin until while repeat variable constant create allot")
            .funcoes(". .\" cr emit dup drop swap over rot + - * / mod bye")
            .modelo(".\" Ola, mundo!\" cr\nbye\n")
            .rodar("gforth {arquivo}")
            .precisa("Gforth: https://gforth.org");

        // 75. ZSH
        nova(EXTRA, "Zsh", "zsh")
            .linha("#").aspas("\"'").variaveis("$")
            .palavras("if then else elif fi case esac for while until do done in function return local export")
            .funcoes("echo print printf read cd ls")
            .modelo("#!/bin/zsh\necho \"Olá, mundo!\"\n")
            .rodar("zsh {arquivo}")
            .precisa("Zsh (Linux/WSL/Mac).")
            .atalho("echo", "mostra", "echo \"$|\"");

        // 76. FISH
        nova(EXTRA, "Fish", "fish")
            .linha("#").aspas("\"'").variaveis("$")
            .palavras("if else end for in while function return set switch case and or not begin")
            .funcoes("echo printf read cd ls")
            .modelo("echo \"Olá, mundo!\"\n")
            .rodar("fish {arquivo}")
            .precisa("Fish shell (Linux/WSL/Mac): https://fishshell.com")
            .atalho("echo", "mostra", "echo \"$|\"");

        // 77. MAKEFILE
        Linguagem make = nova(EXTRA, "Makefile", "mk", "make")
            .linha("#").aspas("\"'").variaveis("$")
            .palavras("all clean install ifeq ifneq else endif include define endef .PHONY")
            .funcoes("echo @echo")
            .modelo("all:\n\t@echo Ola, mundo!\n")
            .rodar("make -f {arquivo}")
            .precisa("make (vem no MSYS2): https://www.msys2.org");
        Catalogo.nomeDeArquivo("Makefile", make);

        // 78. CMAKE
        nova(EXTRA, "CMake", "cmake")
            .semCaixa().linha("#").aspas("\"").variaveis("$")
            .palavras("if else elseif endif foreach endforeach while endwhile function endfunction macro endmacro set "
                    + "project add_executable add_library target_link_libraries cmake_minimum_required")
            .funcoes("message")
            .modelo("message(\"Olá, mundo!\")\n")
            .rodar("cmake -P {arquivo}")
            .precisa("CMake: https://cmake.org/download");

        // 79. DOCKERFILE
        Linguagem docker = nova(EXTRA, "Dockerfile", "dockerfile")
            .semCaixa().linha("#").aspas("\"'")
            .palavras("FROM RUN CMD LABEL EXPOSE ENV ADD COPY ENTRYPOINT VOLUME USER WORKDIR ARG ONBUILD HEALTHCHECK SHELL AS")
            .modelo("FROM alpine\nCMD [\"echo\", \"Olá, mundo!\"]\n")
            .rodar("docker build -f {arquivo} -t tazzcode-teste {pasta} && docker run --rm tazzcode-teste")
            .precisa("Docker Desktop: https://www.docker.com/products/docker-desktop");
        Catalogo.nomeDeArquivo("Dockerfile", docker);

        // 80. YAML
        nova(EXTRA, "YAML", "yml", "yaml")
            .linha("#").aspas("\"'")
            .palavras("true false null yes no on off")
            .modelo("nome: TazzCode\nversao: 1\nlinguagens:\n  - Java\n  - Python\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("YAML é um arquivo de configuração: não roda, só guarda dados.");

        // 81. TOML
        nova(EXTRA, "TOML", "toml")
            .linha("#").aspas("\"'")
            .palavras("true false")
            .modelo("[projeto]\nnome = \"TazzCode\"\nversao = 1\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("TOML é um arquivo de configuração: não roda, só guarda dados.");

        // 82. JSON
        nova(EXTRA, "JSON", "json")
            .aspas("\"")
            .palavras("true false null")
            .modelo("{\n    \"nome\": \"TazzCode\",\n    \"versao\": 1,\n    \"legal\": true\n}\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("JSON guarda dados: não roda. Dá pra ler ele com Python, Java, JavaScript...");

        // 83. XML / SVG
        nova(EXTRA, "XML / SVG", "xml", "svg")
            .estilo(Linguagem.Estilo.HTML).modo(Linguagem.Modo.NAVEGADOR)
            .modelo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\">\n"
                    + "    <circle cx=\"100\" cy=\"100\" r=\"80\" fill=\"#2F7DF6\"/>\n</svg>\n")
            .precisa("Nada: abre no navegador.");

        // 84. MARKDOWN
        nova(EXTRA, "Markdown", "md", "markdown")
            .aspas("`")
            .modelo("# Olá, mundo!\n\nEsse é um texto em **Markdown**.\n\n- item 1\n- item 2\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("Markdown é texto formatado (tipo o LEIA-ME): não roda.");

        // 85. LATEX
        nova(EXTRA, "LaTeX", "tex")
            .linha("%").aspas("")
            .palavras("\\documentclass \\begin \\end \\section \\subsection \\usepackage \\title \\author \\maketitle "
                    + "\\textbf \\textit \\item")
            .letras("\\")
            .modelo("\\documentclass{article}\n\\begin{document}\nOlá, mundo!\n\\end{document}\n")
            .rodar("pdflatex -interaction=nonstopmode -output-directory={pasta} {arquivo}")
            .precisa("MiKTeX: https://miktex.org/download (gera um PDF na mesma pasta)");

        // 86. GLSL
        nova(EXTRA, "GLSL (shader)", "glsl", "vert", "frag")
            .linha("//").bloco("/*", "*/").aspas("\"")
            .palavras("uniform in out inout attribute varying const if else for while return void struct layout "
                    + "precision highp mediump lowp discard")
            .funcoes("sin cos mix clamp dot normalize length texture smoothstep step fract abs")
            .tipos("float int bool vec2 vec3 vec4 mat3 mat4 sampler2D")
            .modelo("#version 330 core\nout vec4 cor;\n\nvoid main() {\n    cor = vec4(0.18, 0.49, 0.96, 1.0);\n}\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("Shaders rodam na placa de vídeo, dentro de um jogo ou motor. Teste online: https://www.shadertoy.com");

        // 87. HLSL
        nova(EXTRA, "HLSL (shader)", "hlsl", "fx")
            .linha("//").bloco("/*", "*/").aspas("\"")
            .palavras("struct return if else for while cbuffer register static const in out inout")
            .funcoes("mul saturate lerp dot normalize sin cos")
            .tipos("float float2 float3 float4 float4x4 int bool Texture2D SamplerState")
            .modelo("float4 main() : SV_TARGET {\n    return float4(0.18, 0.49, 0.96, 1.0);\n}\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("HLSL é o shader do DirectX/Unity: roda dentro de um motor de jogo.");

        // 88. WEBASSEMBLY TEXTO
        nova(EXTRA, "WebAssembly (texto)", "wat", "wast")
            .linha(";;").bloco("(;", ";)").aspas("\"")
            .palavras("module func param result local export import memory data call return block loop br br_if if then else end")
            .tipos("i32 i64 f32 f64")
            .letras(".")
            .modelo("(module\n  (func (export \"_start\")\n    nop)\n)\n")
            .rodar("wasmtime {arquivo}")
            .precisa("Wasmtime: https://wasmtime.dev");

        // 89. GRAPHQL
        nova(EXTRA, "GraphQL", "graphql", "gql")
            .linha("#").aspas("\"")
            .palavras("query mutation subscription fragment on type input enum interface union schema scalar extend")
            .tipos("String Int Float Boolean ID")
            .modelo("type Query {\n  ola: String\n}\n\nquery {\n  ola\n}\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("GraphQL conversa com um servidor de API: não roda sozinho.");

        // 90. TERRAFORM / HCL
        nova(EXTRA, "Terraform (HCL)", "tf", "hcl")
            .linha("#", "//").bloco("/*", "*/").aspas("\"")
            .palavras("resource variable output provider module data locals terraform for_each count true false")
            .modelo("output \"mensagem\" {\n  value = \"Olá, mundo!\"\n}\n")
            .rodar("terraform -chdir={pasta} init && terraform -chdir={pasta} apply -auto-approve")
            .precisa("Terraform: https://developer.hashicorp.com/terraform/install");

        // 91. ARDUINO
        nova(EXTRA, "Arduino", "ino")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("void int float bool char byte if else for while return const HIGH LOW INPUT OUTPUT true false")
            .funcoes("setup loop pinMode digitalWrite digitalRead analogRead analogWrite delay millis Serial.begin "
                    + "Serial.println Serial.print")
            .letras(".")
            .modelo("void setup() {\n  Serial.begin(9600);\n  Serial.println(\"Olá, mundo!\");\n}\n\nvoid loop() {\n}\n")
            .rodar("arduino-cli compile --fqbn arduino:avr:uno {pasta}")
            .precisa("arduino-cli: https://arduino.github.io/arduino-cli (a pasta tem que ter o mesmo nome do .ino)")
            .atalho("serial", "Serial.println", "Serial.println($|);");

        // 92. PROCESSING
        nova(EXTRA, "Processing", "pde")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("void int float boolean String if else for while return class new true false")
            .funcoes("setup draw size background fill stroke ellipse rect line text println random mouseX mouseY")
            .modelo("void setup() {\n  size(400, 400);\n}\n\nvoid draw() {\n  background(27, 34, 51);\n  fill(47, 125, 246);\n  ellipse(mouseX, mouseY, 50, 50);\n}\n")
            .rodar("processing-java --sketch={pasta} --run")
            .precisa("Processing: https://processing.org/download (a pasta tem que ter o mesmo nome do .pde)");

        // 93. LOGO
        nova(EXTRA, "Logo", "logo", "lgo")
            .semCaixa().linha(";").aspas("\"")
            .palavras("to end repeat if ifelse make")
            .funcoes("forward fd back bk left lt right rt penup pu pendown pd print clearscreen cs")
            .modelo("; Logo: a tartaruga que desenha!\nrepeat 4 [forward 100 right 90]\n")
            .link("https://www.calormen.com/jslogo")
            .precisa("Abre um Logo no navegador: cole o código lá e veja a tartaruga desenhar.");

        // 94. BASIC (FreeBASIC)
        nova(EXTRA, "BASIC (FreeBASIC)", "bas", "bi")
            .semCaixa().linha("'", "REM").aspas("\"")
            .palavras("DIM AS IF THEN ELSE ELSEIF END FOR TO STEP NEXT WHILE WEND DO LOOP UNTIL SUB FUNCTION RETURN "
                    + "SELECT CASE GOTO GOSUB AND OR NOT")
            .funcoes("PRINT INPUT LEN MID LEFT RIGHT STR VAL RND INT CLS SLEEP")
            .tipos("INTEGER LONG DOUBLE SINGLE STRING")
            .modelo("PRINT \"Ola, mundo!\"\n")
            .rodar("fbc {arquivo} -x {saida} && {saida}")
            .precisa("FreeBASIC: https://www.freebasic.net/get")
            .atalho("print", "mostra", "PRINT $|")
            .atalho("for", "para", "FOR I = 1 TO $|\n    \nNEXT I");

        // 95. VBSCRIPT
        nova(EXTRA, "VBScript", "vbs")
            .semCaixa().linha("'", "REM").aspas("\"")
            .palavras("Dim Set If Then Else ElseIf End For Each Next To Step Do Loop While Wend Until Sub Function "
                    + "Call Exit Select Case True False Nothing And Or Not Mod")
            .funcoes("WScript.Echo MsgBox InputBox CreateObject Len Mid Left Right UCase LCase CInt CStr Now")
            .letras(".")
            .modelo("WScript.Echo \"Olá, mundo!\"\n")
            .rodar("cscript //nologo {arquivo}")
            .precisa("Já vem no Windows!")
            .atalho("echo", "mostra", "WScript.Echo $|")
            .atalho("msgbox", "caixa", "MsgBox \"$|\"");

        // 96. BRAINFUCK
        nova(EXTRA, "Brainfuck", "bf", "b")
            .modo(Linguagem.Modo.BRAINFUCK)
            .modelo("Olá mundo em Brainfuck (só os 8 símbolos + - < > [ ] . , contam):\n"
                    + "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.\n")
            .precisa("Nada! O TazzCode tem um interpretador de Brainfuck embutido.");

        // 97. LEAN
        nova(EXTRA, "Lean", "lean")
            .linha("--").bloco("/-", "-/").aspas("\"")
            .palavras("def theorem lemma example fun let in if then else match with do return where structure class "
                    + "instance namespace open import by")
            .funcoes("IO.println println! toString")
            .tipos("Nat Int String Bool IO Unit List")
            .letras(".!")
            .modelo("def main : IO Unit :=\n  IO.println \"Olá, mundo!\"\n")
            .rodar("lean --run {arquivo}")
            .precisa("Lean 4: https://lean-lang.org/install");

        // 98. JANET
        nova(EXTRA, "Janet", "janet")
            .linha("#").aspas("\"`")
            .palavras("def var defn fn let if do when unless cond case loop each for while break")
            .funcoes("print printf pp string map filter reduce length")
            .letras("-?!*")
            .modelo("(print \"Olá, mundo!\")\n")
            .rodar("janet {arquivo}")
            .precisa("Janet: https://janet-lang.org");

        // 99. BALLERINA
        nova(EXTRA, "Ballerina", "bal")
            .linha("//").aspas("\"")
            .palavras("import public function returns return if else foreach in while var int string boolean float "
                    + "record service resource check error")
            .funcoes("io:println")
            .letras(":")
            .modelo("import ballerina/io;\n\npublic function main() {\n    io:println(\"Olá, mundo!\");\n}\n")
            .rodar("bal run {arquivo}")
            .precisa("Ballerina: https://ballerina.io/downloads");

        // 100. VALA
        nova(EXTRA, "Vala", "vala")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("class namespace public private static void if else for foreach while return new var using "
                    + "signal async yield true false null")
            .funcoes("print stdout.printf")
            .tipos("int double string bool")
            .modelo("void main() {\n    print(\"Olá, mundo!\\n\");\n}\n")
            .rodar("vala {arquivo}")
            .precisa("Vala (Linux/MSYS2): https://vala.dev");
    }
}
