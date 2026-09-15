@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Compilador - Script de Compilación (Windows)
echo ===================================================

if not exist bin mkdir bin
if not exist output mkdir output

echo [1/2] Compilando codigo fuente Java...
javac -d bin -cp "lib/*" src/com/compilador/*.java src/com/compilador/ast/*.java src/com/compilador/backend/*.java src/com/compilador/herramientas/*.java src/com/compilador/lexico/*.java src/com/compilador/semantico/*.java src/com/compilador/sintactico/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Error durante la compilacion.
    exit /b %ERRORLEVEL%
)

echo [OK] Compilacion completada con exito.
echo.

set INPUT_FILE=%1
if "%INPUT_FILE%"=="" set INPUT_FILE=input\prueba_definitiva.txt

echo [2/2] Ejecutando compilador con: %INPUT_FILE%
echo ---------------------------------------------------
java -cp "lib/*;bin" com.compilador.Main "%INPUT_FILE%"

echo.
echo ===================================================
echo Procesamiento finalizado. Revisa la carpeta output/
echo ===================================================
