package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para sentencias de asignación
 */
public class NodoAsignacion extends NodoSentencia {
    private String variable;
    private NodoExpresion expresion;
    
    public NodoAsignacion(String var, NodoExpresion exp, int linea) {
        super(linea);
        this.variable = var;
        this.expresion = exp;
    }
    
    public String getVariable() {
        return variable;
    }
    
    public NodoExpresion getExpresion() {
        return expresion;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        String temp = expresion.generarCodigo(gen);
        gen.emitirAsignacion(variable, temp);
        return null;
    }
    
    @Override
    public String toString() {
        return "Asignacion(" + variable + " = " + expresion + ")";
    }
}
