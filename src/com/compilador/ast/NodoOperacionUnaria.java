package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para operaciones unarias (!, -, +)
 */
public class NodoOperacionUnaria extends NodoExpresion {
    private NodoExpresion operando;
    private String operador;
    
    public NodoOperacionUnaria(String op, NodoExpresion operando, int linea) {
        super(linea);
        this.operador = op;
        this.operando = operando;
    }
    
    public NodoExpresion getOperando() {
        return operando;
    }
    
    public String getOperador() {
        return operador;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        String temp = operando.generarCodigo(gen);
        return gen.emitirOperacionUnaria(operador, temp);
    }
    
    @Override
    public String toString() {
        return "OpUnaria(" + operador + " " + operando + ")";
    }
}
