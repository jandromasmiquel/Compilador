package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Clase base para nodos de expresiones
 */
public abstract class NodoExpresion extends Nodo {
    
    public NodoExpresion(int linea) {
        super(linea);
    }
    
    @Override
    public abstract String generarCodigo(GeneradorCodigo gen);
}
