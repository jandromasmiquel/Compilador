package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para sentencias return/devolver
 */
public class NodoDevolver extends NodoSentencia {
    private NodoExpresion expresion;
    
    public NodoDevolver(NodoExpresion exp, int linea) {
        super(linea);
        this.expresion = exp;
    }
    
    public NodoExpresion getExpresion() {
        return expresion;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        if (expresion != null) {
            String temp = expresion.generarCodigo(gen);
            gen.emitirRetorno(temp);
        } else {
            gen.emitirRetorno("");
        }
        return null;
    }
}
