package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import com.compilador.semantico.TipoDato;

/**
 * Nodo para literales numéricos
 */
public class NodoNumero extends NodoExpresion {
    private int valor;
    
    public NodoNumero(int valor, int linea) {
        super(linea);
        this.valor = valor;
        this.tipo = TipoDato.ENTERO;
    }
    
    public int getValor() {
        return valor;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        return String.valueOf(valor);
    }
    
    @Override
    public String toString() {
        return "Numero(" + valor + ")";
    }
}
