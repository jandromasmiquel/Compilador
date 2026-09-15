package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import com.compilador.semantico.TipoDato;

public class NodoLeer extends NodoSentencia {
    private String variable;
    private TipoDato tipo;

    public NodoLeer(String variable, TipoDato tipo, int linea) {
        super(linea);
        this.variable = variable;
        this.tipo = tipo;
    }

    public String getVariable() {
        return variable;
    }

    public TipoDato getTipo() {
        return tipo;
    }

    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        // Generar código de lectura según el tipo
        switch (tipo) {
            case ENTERO:
                gen.emitir("read_entero " + variable);
                break;
            case CADENA:
                gen.emitir("read_cadena " + variable);
                break;
            case CARACTER:
                gen.emitir("read_caracter " + variable);
                break;
            case BOOLEANO:
                gen.emitir("read_booleano " + variable);
                break;
            default:
                gen.emitir("# ERROR: tipo no soportado para lectura");
        }
        return null;
    }
}
