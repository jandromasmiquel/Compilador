package com.compilador.herramientas;

/**
 * Clase auxiliar para representar un token
 * Útil si no se usa la clase Symbol de CUP
 */
public class Token {
    
    private String tipo;
    private String lexema;
    private int linea;
    private int columna;
    private Object valor;
    
    public Token(String tipo, String lexema, int linea, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.valor = null;
    }
    
    public Token(String tipo, String lexema, int linea, int columna, Object valor) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.valor = valor;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public String getLexema() {
        return lexema;
    }
    
    public int getLinea() {
        return linea;
    }
    
    public int getColumna() {
        return columna;
    }
    
    public Object getValor() {
        return valor;
    }
    
    @Override
    public String toString() {
        if (valor != null) {
            return "<%s, '%s', %d:%d, valor=%s>".formatted(
            tipo, lexema, linea, columna, valor);
        }
        return "<%s, '%s', %d:%d>".formatted(tipo, lexema, linea, columna);
    }
    
    /**
     * Representación compacta del token
     */
    public String toStringCompacto() {
        return "%-15s %-20s [%d:%d]".formatted(tipo, lexema, linea, columna);
    }
}
