#!/usr/bin/env bash
# Usado pelos outros .sh: acha o Java e compila o TazzCode.
cd "$(dirname "$0")"

# 1) Acha o javac (o JDK)
if command -v javac >/dev/null 2>&1; then
    JAVA_BIN="$(dirname "$(readlink -f "$(command -v javac)")")"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
    JAVA_BIN="$JAVA_HOME/bin"
else
    echo
    echo "[ERRO] Não achei o \"javac\" (o JDK). Instale com:"
    echo "    sudo apt install openjdk-21-jdk"
    exit 1
fi
export JAVA_BIN
echo "Usando o Java de: $JAVA_BIN"

# 2) Confere se todos os arquivos estão aqui
FALTOU=0
for f in AbaEditor.java AjudanteOffline.java Ajustes.java App.java Autocompletar.java BarraRolagem.java BotaoModerno.java Brainfuck.java CaixaEscolha.java Catalogo.java CatalogoEspeciais.java CatalogoExtras.java CatalogoOneCompiler.java CatalogoPrincipais.java ClienteClaude.java ClienteLocal.java Config.java Console.java Destaque.java EditorCodigo.java Executaveis.java FazerIcone.java Historico.java Icone.java Icones.java JanelaBanco.java JanelaConfiguracoes.java JanelaEditor.java JanelaInicial.java JanelaPlugins.java JanelaTutorial.java Json.java Linguagem.java Linguagens.java Main.java Modelos.java ModelosProjeto.java MongoSimulado.java MotorBanco.java NumerosLinha.java PainelHelper.java PainelProblemas.java PainelProjeto.java Pastas.java Plugins.java Projeto.java RedisSimulado.java Reiniciar.java ServidorLocal.java Sql.java Tema.java VerificadorHtml.java Vigia.java; do
    if [ ! -f "src/tazzcode/$f" ]; then echo "[FALTANDO] src/tazzcode/$f"; FALTOU=1; fi
done
if [ $FALTOU = 1 ]; then
    echo
    echo "[ERRO] Estão faltando arquivos. Extraia o zip INTEIRO de novo numa pasta nova."
    exit 1
fi

# 3) Compila
rm -rf out && mkdir out
"$JAVA_BIN/javac" -encoding UTF-8 -d out src/tazzcode/*.java || exit 1
[ -f recursos/icone.png ] && cp recursos/icone.png out/icone.png
exit 0
