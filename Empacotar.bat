@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ============================================================
echo   EMPACOTAR O TAZZCODE
echo   1) TazzCode.exe (com o Java dentro: nao precisa instalar Java)
echo   2) Instalador (Instalar-TazzCode.exe) que coloca no Program Files
echo ============================================================
echo.

call _compilar.bat
if errorlevel 1 goto erro

rem Pega a versao de dentro do App.java (ex.: 0.7)
for /f tokens^=2^ delims^=^" %%v in ('findstr /c:"VERSAO =" src\tazzcode\App.java') do set "VERSAO=%%v"
echo Versao: %VERSAO%

if exist dist rmdir /s /q dist
mkdir dist\entrada

echo.
echo [1/4] Criando o TazzCode.jar...
"%JAVA_BIN%jar.exe" --create --file dist\entrada\TazzCode.jar --main-class tazzcode.Main -C out .
if errorlevel 1 goto erro

echo [2/4] Copiando a IA (pasta ia)...
robocopy ia dist\entrada\ia /E /XD __pycache__ /NFL /NDL /NJH /NJS /NP >nul
if errorlevel 8 goto erro

echo [3/4] Fazendo o icone (.ico)...
"%JAVA_BIN%java.exe" -cp out tazzcode.FazerIcone recursos\icone.png dist\icone.ico
if errorlevel 1 goto erro

echo [4/4] Criando o TazzCode.exe com o jpackage (demora um pouquinho)...
"%JAVA_BIN%jpackage.exe" --type app-image --name TazzCode --app-version %VERSAO% ^
    --input dist\entrada --main-jar TazzCode.jar --main-class tazzcode.Main ^
    --icon dist\icone.ico --dest dist\app --vendor "Joao" --description "Editor de codigo TazzCode" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --add-modules java.base,java.desktop,java.net.http,jdk.httpserver,jdk.charsets,jdk.localedata ^
    --jlink-options "--strip-debug --no-header-files --no-man-pages --include-locales=pt-BR,en"
if errorlevel 1 goto erro
echo.
echo Pronto: dist\app\TazzCode\TazzCode.exe  (essa pasta inteira e o programa)

rem ---------- O instalador (precisa do Inno Setup, que e gratis) ----------
set "ISCC="
for %%p in ("%ProgramFiles(x86)%\Inno Setup 6\ISCC.exe" "%ProgramFiles%\Inno Setup 6\ISCC.exe" "%ProgramFiles(x86)%\Inno Setup 7\ISCC.exe" "%ProgramFiles%\Inno Setup 7\ISCC.exe" "%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe") do if exist %%p if not defined ISCC set "ISCC=%%~p"
if not defined ISCC (
    echo.
    echo O instalador NAO foi criado porque falta o Inno Setup ^(gratis^).
    echo Peca pra um adulto instalar: winget install JRSoftware.InnoSetup
    echo ^(ou baixe em jrsoftware.org^) e rode este .bat de novo.
    goto fim
)
echo.
echo Criando o instalador com o Inno Setup...
"%ISCC%" /Q /DVersao=%VERSAO% instalador\TazzCode.iss
if errorlevel 1 goto erro
if not exist site\downloads mkdir site\downloads
copy /y dist\Instalar-TazzCode-%VERSAO%.exe site\downloads\ >nul
echo.
echo ============================================================
echo   PRONTO! O instalador ficou em: dist\Instalar-TazzCode-%VERSAO%.exe
echo   ^(e uma copia em site\downloads, pro site de download^)
echo ============================================================
goto fim

:erro
echo.
echo Deu erro! Tire um print das mensagens acima e mande pro Claude.
:fim
pause
