package com.compilador.ast;

import com.compilador.semantico.TipoDato;
import com.compilador.backend.GeneradorCodigo;

/**
 * Clase base para todos los nodos del AST
 */
public abstract class Nodo {
    protected int linea;
    protected TipoDato tipo;
    
    public Nodo(int linea) {
        this.linea = linea;
        this.tipo = TipoDato.ERROR;
    }
    
    public int getLinea() {
        return linea;
    }
    
    public TipoDato getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoDato tipo) {
        this.tipo = tipo;
    }
    
    /**
     * Genera código intermedio para este nodo
     * @return Temporal o variable donde queda el resultado
     */
    public abstract String generarCodigo(GeneradorCodigo gen);
}
