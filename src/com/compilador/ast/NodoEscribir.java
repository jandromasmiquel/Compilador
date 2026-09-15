package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para sentencias print/escribir
 */
public class NodoEscribir extends NodoSentencia {
    private NodoExpresion expresion;
    
    public NodoEscribir(NodoExpresion exp, int linea) {
        super(linea);
        this.expresion = exp;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        String temp = expresion.generarCodigo(gen);
        String tipoStr = expresion.getTipo().toString();
        gen.emitir("print_" + tipoStr + " " + temp);
        return null;
    }
}
