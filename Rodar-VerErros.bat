@echo off
cd /d "%~dp0"
echo Compilando o TazzCode (modo ver erros)...
call _compilar.bat
if errorlevel 1 (
    echo.
    echo Deu erro na compilacao! Tire um print desta janela e mande pro Claude.
    pause
    exit /b 1
)
echo Compilou! Abrindo o editor... (deixe esta janela aberta)
echo.
"%JAVA_BIN%java.exe" -cp out tazzcode.Main %*
echo.
echo O editor fechou. Se apareceu algum erro acima, tire um print e mande pro Claude.
pause
