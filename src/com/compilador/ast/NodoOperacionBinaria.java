package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para operaciones binarias (+, -, *, /, <, >, ==, etc.)
 */
public class NodoOperacionBinaria extends NodoExpresion {
    private NodoExpresion izquierda;
    private NodoExpresion derecha;
    private String operador;
    
    public NodoOperacionBinaria(NodoExpresion izq, String op, NodoExpresion der, int linea) {
        super(linea);
        this.izquierda = izq;
        this.operador = op;
        this.derecha = der;
    }
    
    public NodoExpresion getIzquierda() {
        return izquierda;
    }
    
    public NodoExpresion getDerecha() {
        return derecha;
    }
    
    public String getOperador() {
        return operador;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        String temp1 = izquierda.generarCodigo(gen);
        String temp2 = derecha.generarCodigo(gen);
        return gen.emitirOperacion(temp1, operador, temp2);
    }
    
    @Override
    public String toString() {
        return "OpBinaria(" + izquierda + " " + operador + " " + derecha + ")";
    }
}
