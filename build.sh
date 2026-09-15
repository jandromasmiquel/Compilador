#!/bin/bash

echo "==================================================="
echo "  Compilador - Script de Compilación (Linux/macOS)"
echo "==================================================="

mkdir -p bin output

echo "[1/2] Compilando código fuente Java..."
find src -name "*.java" > sources.txt
javac -d bin -cp "lib/*" @sources.txt
STATUS=$?
rm -f sources.txt

if [ $STATUS -ne 0 ]; then
    echo "[ERROR] Error durante la compilación."
    exit 1
fi

echo "[OK] Compilación completada con éxito."
echo ""

INPUT_FILE=${1:-input/prueba_definitiva.txt}

echo "[2/2] Ejecutando compilador con: $INPUT_FILE"
echo "---------------------------------------------------"
java -cp "lib/*:bin" com.compilador.Main "$INPUT_FILE"

echo ""
echo "==================================================="
echo "Procesamiento finalizado. Revisa la carpeta output/"
echo "==================================================="
