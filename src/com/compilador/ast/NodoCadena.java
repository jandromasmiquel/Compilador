package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import com.compilador.semantico.TipoDato;

/**
 * Nodo para literales de cadena
 */
public class NodoCadena extends NodoExpresion {
    private String valor;
    
    public NodoCadena(String valor, int linea) {
        super(linea);
        this.valor = valor;
        this.tipo = TipoDato.CADENA;
    }
    
    public String getValor() {
        return valor;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        // Registrar la cadena y devolver su etiqueta
        String cadenaLimpia = valor.replace("\"", "");
        return gen.registrarCadena(cadenaLimpia);
    }
    
    @Override
    public String toString() {
        return "Cadena(" + valor + ")";
    }
}
