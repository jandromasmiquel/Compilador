package com.compilador.backend;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generador de código intermedio de tres direcciones
 * Produce instrucciones en formato: resultado = operando1 op operando2
 */
public class GeneradorCodigo {
    
    private List<String> codigoIntermedio;
    private int contadorTemporales;
    private int contadorEtiquetas;
    private Map<String, String> cadenasLiterales;
    private int contadorCadenas;
    
    public GeneradorCodigo() {
        this.codigoIntermedio = new ArrayList<>();
        this.contadorTemporales = 0;
        this.contadorEtiquetas = 0;
        this.cadenasLiterales = new HashMap<>();
        this.contadorCadenas = 0;
    }
    
    /**
     * Genera un nuevo temporal
     */
    public String nuevoTemporal() {
        return "t" + (contadorTemporales++);
    }
    
    /**
     * Genera una nueva etiqueta
     */
    public String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }
    
    /**
     * Registra una cadena literal y devuelve su etiqueta
     */
    public String registrarCadena(String cadena) {
        if (!cadenasLiterales.containsKey(cadena)) {
            String etiqueta = "str_" + contadorCadenas++;
            cadenasLiterales.put(cadena, etiqueta);
        }
        return cadenasLiterales.get(cadena);
    }
    
    /**
     * Emite una instrucción de tres direcciones
     */
    public void emitir(String instruccion) {
        codigoIntermedio.add(instruccion);
    }
    
    /**
     * Emite una operación aritmética
     */
    public String emitirOperacion(String op1, String operador, String op2) {
        String temp = nuevoTemporal();
        emitir(temp + " = " + op1 + " " + operador + " " + op2);
        return temp;
    }
    
    /**
     * Emite una operación unaria
     */
    public String emitirOperacionUnaria(String operador, String operando) {
        String temp = nuevoTemporal();
        emitir(temp + " = " + operador + operando);
        return temp;
    }

    /**
     * Emite una asignación
     */
    public void emitirAsignacion(String destino, String fuente) {
        emitir(destino + " = " + fuente);
    }
    
    /**
     * Emite una etiqueta
     */
    public void emitirEtiqueta(String etiqueta) {
        emitir(etiqueta + ":");
    }
    
    /**
     * Emite un salto incondicional
     */
    public void emitirSalto(String etiqueta) {
        emitir("goto " + etiqueta);
    }
    
    /**
     * Emite un salto condicional
     */
    public void emitirSaltoCondicional(String condicion, String etiqueta) {
        emitir("if " + condicion + " goto " + etiqueta);
    }
    
    /**
     * Emite un salto condicional falso
     */
    public void emitirSaltoCondicionalFalso(String condicion, String etiqueta) {
        emitir("ifFalse " + condicion + " goto " + etiqueta);
    }
    
    /**
     * Emite una llamada a función
     */
    public void emitirLlamada(String funcion, int numParametros) {
        emitir("call " + funcion + ", " + numParametros);
    }
    
    /**
     * Emite un retorno de función
     */
    public void emitirRetorno(String valor) {
        emitir("return " + valor);
    }
    
    /**
     * Emite un parámetro de función
     */
    public void emitirParametro(String param) {
        emitir("param " + param);
    }
    

    
    /**
     * Volca el código intermedio a un archivo
     */
    public void volcarAArchivo(String rutaArchivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            writer.println("========================================");
            writer.println("     CÓDIGO INTERMEDIO (3 DIRECCIONES)");
            writer.println("========================================");
            writer.println();
            
            for (int i = 0; i < codigoIntermedio.size(); i++) {
                writer.printf("%4d:  %s%n", i, codigoIntermedio.get(i));
            }
            
            writer.println();
            writer.println("========================================");
        }
    }
    
    /**
     * Imprime el código intermedio en consola
     */
    public void imprimir() {
        System.out.println("\n=== CÓDIGO INTERMEDIO ===");
        for (int i = 0; i < codigoIntermedio.size(); i++) {
            System.out.printf("%4d:  %s%n", i, codigoIntermedio.get(i));
        }
    }
    
    /**
     * Obtiene la lista de instrucciones
     */
    public List<String> getInstrucciones() {
        return codigoIntermedio;
    }
    
    /**
     * Obtiene el mapa de cadenas literales
     */
    public Map<String, String> getCadenasLiterales() {
        return cadenasLiterales;
    }
}
