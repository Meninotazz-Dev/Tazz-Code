@echo off
cd /d "%~dp0"
if not exist src\tazzcode (
    echo [ERRO] Nao achei a pasta src. Extraia o zip inteiro antes de rodar
    echo ^(botao direito no zip ^> Extrair tudo^).
    pause
    exit /b 1
)
echo Compilando o TazzCode...
call _compilar.bat
if errorlevel 1 (
    echo.
    echo Deu erro na compilacao! Tire um print desta janela e mande pro Claude.
    pause
    exit /b 1
)
echo Compilou! Abrindo o editor...
echo (se o editor nao aparecer, use o "Rodar-VerErros.bat")
start "" "%JAVA_BIN%javaw.exe" -cp out tazzcode.Main %*
timeout /t 3 >nul
