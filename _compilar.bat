@echo off
rem Esse arquivo e usado pelos outros .bat. Ele acha o Java e compila.
cd /d "%~dp0"

rem 1) Acha a pasta do JDK (onde esta o javac). O "java" usado depois vem da MESMA pasta,
rem    pra nao misturar um Java novo com um Java antigo que esteja instalado no PC.
set "JAVA_BIN="
for /f "delims=" %%i in ('where javac 2^>nul') do if not defined JAVA_BIN set "JAVA_BIN=%%~dpi"
if not defined JAVA_BIN if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVA_BIN=%JAVA_HOME%\bin\"
if not defined JAVA_BIN (
    echo.
    echo [ERRO] Nao achei o "javac" neste computador.
    echo Instale o JDK 21 ^(ex.: Eclipse Temurin^) marcando "Add to PATH"
    echo ou configure a variavel JAVA_HOME. Depois abra este .bat de novo.
    exit /b 1
)
echo Usando o Java de: %JAVA_BIN%

rem 2) Confere se todos os arquivos estao aqui (as vezes o zip nao extrai tudo)
set "FALTOU="
for %%f in (AbaEditor.java AjudanteOffline.java Ajustes.java App.java Autocompletar.java BarraRolagem.java BotaoModerno.java Brainfuck.java CaixaEscolha.java Catalogo.java CatalogoEspeciais.java CatalogoExtras.java CatalogoOneCompiler.java CatalogoPrincipais.java ClienteClaude.java ClienteLocal.java Config.java Console.java Destaque.java EditorCodigo.java Executaveis.java FazerIcone.java Historico.java Icone.java Icones.java JanelaBanco.java JanelaConfiguracoes.java JanelaEditor.java JanelaInicial.java JanelaPlugins.java JanelaTutorial.java Json.java Linguagem.java Linguagens.java Main.java Modelos.java ModelosProjeto.java MongoSimulado.java MotorBanco.java NumerosLinha.java PainelHelper.java PainelProblemas.java PainelProjeto.java Pastas.java Plugins.java Projeto.java RedisSimulado.java Reiniciar.java ServidorLocal.java Sql.java Tema.java VerificadorHtml.java Vigia.java) do if not exist "src\tazzcode\%%f" (
    echo [FALTANDO] src\tazzcode\%%f
    set "FALTOU=1"
)
if defined FALTOU (
    echo.
    echo [ERRO] Estao faltando arquivos do TazzCode ^(lista acima^).
    echo Apague esta pasta e extraia o zip INTEIRO de novo numa pasta nova
    echo ^(botao direito no zip ^> Extrair tudo^). Seus projetos NAO somem: eles ficam em %%USERPROFILE%%\TazzCode.
    echo Se o antivirus apagou algum arquivo, olhe a quarentena dele.
    exit /b 1
)

rem 3) Compila
if exist out rmdir /s /q out
mkdir out
"%JAVA_BIN%javac.exe" -encoding UTF-8 -d out src\tazzcode\*.java
if errorlevel 1 exit /b 1
if exist recursos\icone.png copy /y recursos\icone.png out\icone.png >nul
exit /b 0
