package com.compilador.herramientas;

import java.io.*;
import java.util.*;

/**
 * Gestor de errores del compilador
 * Registra y reporta errores léxicos, sintácticos y semánticos
 */
public class GestorErrores {
    
    // Clase interna para representar un error
    private static class Error {
        private int linea;
        private String tipo;
        private String mensaje;
        
        public Error(int linea, String tipo, String mensaje) {
            this.linea = linea;
            this.tipo = tipo;
            this.mensaje = mensaje;
        }
        
        @Override
        public String toString() {
            return "Línea %d [%s]: %s".formatted(linea, tipo, mensaje);
        }
    }
    
    private List<Error> errores;
    private boolean hayErrores;
    
    public GestorErrores() {
        this.errores = new ArrayList<>();
        this.hayErrores = false;
    }
    
    /**
     * Reporta un error léxico
     */
    public void reportarErrorLexico(int linea, String mensaje) {
        errores.add(new Error(linea, "LÉXICO", mensaje));
        hayErrores = true;
        System.err.println("Error léxico en línea " + linea + ": " + mensaje);
    }
    
    /**
     * Reporta un error sintáctico
     */
    public void reportarErrorSintactico(int linea, String mensaje) {
        errores.add(new Error(linea, "SINTÁCTICO", mensaje));
        hayErrores = true;
        System.err.println("Error sintáctico en línea " + linea + ": " + mensaje);
    }
    
    /**
     * Reporta un error semántico
     */
    public void reportarErrorSemantico(int linea, String mensaje) {
        errores.add(new Error(linea, "SEMÁNTICO", mensaje));
        hayErrores = true;
        System.err.println("Error semántico en línea " + linea + ": " + mensaje);
    }
    
    /**
     * Reporta un error genérico
     */
    public void reportarError(int linea, String mensaje) {
        reportarErrorSemantico(linea, mensaje);
    }
    
    /**
     * Verifica si hay errores
     */
    public boolean hayErrores() {
        return hayErrores;
    }
    
    /**
     * Obtiene el número de errores
     */
    public int numeroDeErrores() {
        return errores.size();
    }
    
    /**
     * Volca los errores a un archivo
     */
    public void volcarAArchivo(String rutaArchivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            writer.println("========================================");
            writer.println("       INFORME DE ERRORES");
            writer.println("========================================");
            writer.println();
            
            if (errores.isEmpty()) {
                writer.println("No se encontraron errores.");
            } else {
                writer.println("Total de errores: " + errores.size());
                writer.println();
                
                for (Error error : errores) {
                    writer.println(error);
                }
            }
            
            writer.println();
            writer.println("========================================");
        }
    }
    
    /**
     * Imprime todos los errores en consola
     */
    public void imprimirErrores() {
        System.out.println("\n=== RESUMEN DE ERRORES ===");
        if (errores.isEmpty()) {
            System.out.println("No se encontraron errores.");
        } else {
            System.out.println("Total de errores: " + errores.size());
            for (Error error : errores) {
                System.out.println(error);
            }
        }
    }
    
    /**
     * Limpia todos los errores
     */
    public void limpiar() {
        errores.clear();
        hayErrores = false;
    }
}
