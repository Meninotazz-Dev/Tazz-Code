#!/usr/bin/env bash
# Compila e abre o TazzCode (Linux). Rodar:  ./rodar.sh   (ou: bash rodar.sh)
cd "$(dirname "$0")"
if ! bash _compilar.sh; then
    echo; echo "Deu erro na compilação! Mande um print pro Claude."
    read -r -p "Aperte ENTER pra fechar..." _
    exit 1
fi
JAVA_BIN="$(dirname "$(readlink -f "$(command -v javac || echo "$JAVA_HOME/bin/javac")")")"
exec "$JAVA_BIN/java" -cp out tazzcode.Main "$@"
