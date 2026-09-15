package com.compilador.semantico;

/**
 * Enumeración para los tipos de datos soportados por el compilador
 */
public enum TipoDato {
    ENTERO,
    CADENA,
    CARACTER,
    BOOLEANO,
    VOID,
    ERROR,
    ARRAY; // Tipo especial para arrays
    
    @Override
    public String toString() {
        switch(this) {
            case ENTERO: return "entero";
            case CADENA: return "cadena";
            case CARACTER: return "caracter";
            case BOOLEANO: return "booleano";
            case VOID: return "void";
            case ERROR: return "error";
            case ARRAY: return "array";
            default: return "desconocido";
        }
    }
}
