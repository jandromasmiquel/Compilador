package com.compilador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import com.compilador.ast.NodoPrograma;
import com.compilador.backend.GeneradorCodigo;
import com.compilador.backend.GeneradorEnsamblador;
import com.compilador.backend.Optimizador;
import com.compilador.herramientas.GestorErrores;
import com.compilador.lexico.Scanner;
import com.compilador.semantico.TablaSimbolos;
import com.compilador.sintactico.Parser;

import java_cup.runtime.Symbol;

/**
 * Clase principal del compilador
 * Orquesta todo el proceso: léxico, sintáctico, semántico y generación de código
 */
public class Main {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Uso: java com.compilador.Main <archivo_entrada>");
            System.err.println("Ejemplo: java com.compilador.Main input/prueba_ok.txt");
            System.exit(1);
        }
        
        String archivoEntrada = args[0];
        
        try {
            System.out.println("=== COMPILADOR 2025 ===");
            System.out.println("Procesando: " + archivoEntrada);
            System.out.println();
            
            // Crear directorio output si no existe
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Fase 1: Análisis Léxico (generar tokens.txt)
            System.out.println("[1/4] Análisis Léxico...");
            FileReader reader = new FileReader(archivoEntrada);
            Scanner scanner = new Scanner(reader);
            
            // Leer y guardar todos los tokens
            PrintWriter tokensWriter = new PrintWriter(new FileWriter("output/tokens.txt"));
            tokensWriter.println("========================================");
            tokensWriter.println("       LISTA DE TOKENS");
            tokensWriter.println("========================================");
            tokensWriter.println();
            tokensWriter.printf("%-20s %-20s %-10s %-10s%n", "TOKEN", "LEXEMA", "LINEA", "COLUMNA");
            tokensWriter.println("------------------------------------------------------------");
            
            int tokenCount = 0;
            Symbol token;
            while (true) {
                token = scanner.next_token();
                if (token.sym == 0) break; // EOF
                
                String tokenName = getTokenName(token.sym);
                String lexema = token.value != null ? token.value.toString() : "-";
                tokensWriter.printf("%-20s %-20s %-10d %-10d%n", 
                    tokenName, lexema, token.left + 1, token.right + 1);
                tokenCount++;
            }
            
            tokensWriter.println();
            tokensWriter.println("Total de tokens: " + tokenCount);
            tokensWriter.println("========================================");
            tokensWriter.close();
            System.out.println("      [OK] Tokens guardados en output/tokens.txt (" + tokenCount + " tokens)");
            
            // Fase 2: Análisis Sintáctico y Semántico
            System.out.println("[2/4] Análisis Sintáctico y Semántico...");
            FileReader reader2 = new FileReader(archivoEntrada);
            Scanner scanner2 = new Scanner(reader2);
            @SuppressWarnings("deprecation")
            Parser parser = new Parser(scanner2, new java_cup.runtime.DefaultSymbolFactory());
            
            // Parsear y obtener el AST
            Symbol resultado = parser.parse();
            NodoPrograma programa = (NodoPrograma) resultado.value;
            
            // Obtener tabla de símbolos y gestor de errores del parser
            TablaSimbolos tablaSimbolos = parser.getTablaSimbolos();
            GestorErrores gestorErrores = parser.getGestorErrores();
            
            System.out.println("      [OK] Análisis sintáctico completado");
            System.out.println("      [OK] Análisis semántico completado");
            
            // Guardar tabla de símbolos
            tablaSimbolos.volcarAArchivo("output/tabla_simbolos.txt");
            System.out.println("      [OK] Tabla de símbolos guardada en output/tabla_simbolos.txt");
            
            // Guardar tablas de variables y procedimientos (para verificación del código intermedio)
            tablaSimbolos.volcarTablaVariables("output/tabla_variables.txt");
            System.out.println("      [OK] Tabla de variables guardada en output/tabla_variables.txt");
            
            tablaSimbolos.volcarTablaProcedimientos("output/tabla_procedimientos.txt");
            System.out.println("      [OK] Tabla de procedimientos guardada en output/tabla_procedimientos.txt");
            
            // Si hay errores semánticos, no continuar
            if (gestorErrores.hayErrores()) {
                System.err.println();
                System.err.println("Se encontraron " + gestorErrores.numeroDeErrores() + " errores.");
                gestorErrores.volcarAArchivo("output/errores.txt");
                System.err.println("Detalles en output/errores.txt");
                System.exit(1);
            }
            
            // Fase 3: Generación de Código Intermedio
            System.out.println("[3/4] Generación de Código Intermedio...");
            GeneradorCodigo generadorCodigo = new GeneradorCodigo();
            
            // Inicializar constantes globales
            for (TablaSimbolos.Simbolo simbolo : tablaSimbolos.obtenerTodosSimbolo()) {
                if (simbolo.esConstante() && "global".equals(simbolo.getAmbito())) {
                    generadorCodigo.emitirAsignacion(simbolo.getNombre(), simbolo.getValorConstante().toString());
                }
            }
            
            // Generar código recorriendo el AST
            programa.generarCodigo(generadorCodigo);
            
            generadorCodigo.volcarAArchivo("output/codigo_intermedio.txt");
            System.out.println("      [OK] Código intermedio guardado en output/codigo_intermedio.txt");
            
            // Fase 4: Optimización y Generación de Ensamblador
            System.out.println("[4/4] Optimización y Generación de Ensamblador...");
            
            // Generar ensamblador SIN optimizar
            GeneradorEnsamblador generadorAsmSinOpt = new GeneradorEnsamblador(generadorCodigo.getInstrucciones());
            generadorAsmSinOpt.setCadenasLiterales(generadorCodigo.getCadenasLiterales());
            generadorAsmSinOpt.generar();
            generadorAsmSinOpt.volcarAArchivo("output/codigo_sin_optimizar.asm");
            System.out.println("      [OK] Código sin optimizar guardado en output/codigo_sin_optimizar.asm");
            
            // Optimizar código
            Optimizador optimizador = new Optimizador(generadorCodigo.getInstrucciones());
            int tamanioOriginal = generadorCodigo.getInstrucciones().size();
            List<String> codigoOptimizado = optimizador.optimizar();
            int instruccionesEliminadas = tamanioOriginal - codigoOptimizado.size();
            
            if (instruccionesEliminadas > 0) {
                System.out.println("      [OK] Optimización completada (" + instruccionesEliminadas + " instrucciones eliminadas)");
            }
            
            // Generar ensamblador OPTIMIZADO
            GeneradorEnsamblador generadorAsmOpt = new GeneradorEnsamblador(codigoOptimizado);
            generadorAsmOpt.setCadenasLiterales(generadorCodigo.getCadenasLiterales());
            generadorAsmOpt.generar();
            generadorAsmOpt.volcarAArchivo("output/codigo_optimizado.asm");
            System.out.println("      [OK] Código optimizado guardado en output/codigo_optimizado.asm");
            
            // Generar reporte de errores (vacío si no hay errores)
            gestorErrores.volcarAArchivo("output/errores.txt");
            System.out.println("      [OK] Reporte de errores guardado en output/errores.txt");
            
            System.out.println();
            System.out.println("========================================");
            System.out.println("¡Compilación completada con éxito!");
            System.out.println("========================================");
            System.out.println("Archivos generados en output/:");
            System.out.println("  - tokens.txt (" + tokenCount + " tokens)");
            System.out.println("  - tabla_simbolos.txt");
            System.out.println("  - tabla_variables.txt");
            System.out.println("  - tabla_procedimientos.txt");
            System.out.println("  - codigo_intermedio.txt (" + tamanioOriginal + " instrucciones)");
            System.out.println("  - codigo_sin_optimizar.asm (" + tamanioOriginal + " instrucciones)");
            System.out.println("  - codigo_optimizado.asm (" + codigoOptimizado.size() + " instrucciones)");
            System.out.println("  - errores.txt (sin errores)");
            if (instruccionesEliminadas > 0) {
                System.out.println();
                System.out.println("Optimización: " + instruccionesEliminadas + " instrucciones eliminadas");
                double porcentaje = (instruccionesEliminadas * 100.0) / tamanioOriginal;
                System.out.printf("Reducción: %.2f%%%n", porcentaje);
            }
            System.out.println("========================================");
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: Archivo no encontrado: " + archivoEntrada);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error durante la compilación: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Obtiene el nombre del token a partir de su código
     */
    private static String getTokenName(int sym) {
        switch (sym) {
            case 2: return "ENTERO_TIPO";
            case 3: return "CADENA_TIPO";
            case 4: return "BOOLEANO_TIPO";
            case 5: return "CARACTER_TIPO";
            case 6: return "VACIO_TIPO";
            case 7: return "CONSTANTE";
            case 8: return "VERDADERO";
            case 9: return "FALSO";
            case 10: return "SI";
            case 11: return "SINO";
            case 12: return "MIENTRAS";
            case 13: return "PARA";
            case 14: return "DEVOLVER";
            case 15: return "ESCRIBIR";
            case 16: return "LEER";
            case 17: return "MAS";
            case 18: return "MENOS";
            case 19: return "POR";
            case 20: return "DIV";
            case 21: return "ASIGNAR";
            case 22: return "IGUAL";
            case 23: return "DIFERENTE";
            case 24: return "MENOR";
            case 25: return "MAYOR";
            case 26: return "MENORIGUAL";
            case 27: return "MAYORIGUAL";
            case 28: return "AND";
            case 29: return "OR";
            case 30: return "NOT";
            case 31: return "PAREN_IZQ";
            case 32: return "PAREN_DER";
            case 33: return "LLAVE_IZQ";
            case 34: return "LLAVE_DER";
            case 35: return "CORCHETE_IZQ";
            case 36: return "CORCHETE_DER";
            case 37: return "PUNTO";
            case 38: return "PUNTO_COMA";
            case 39: return "COMA";
            case 40: return "NUMERO";
            case 41: return "CADENA";
            case 42: return "ID";
            case 43: return "CARACTER_LIT";
            default: return "UNKNOWN";
        }
    }
}
