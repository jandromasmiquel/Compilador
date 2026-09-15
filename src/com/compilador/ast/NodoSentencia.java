package com.compilador.ast;

/**
 * Clase base para nodos de sentencias
 */
public abstract class NodoSentencia extends Nodo {
    
    public NodoSentencia(int linea) {
        super(linea);
    }
}
