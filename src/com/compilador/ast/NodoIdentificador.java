package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para identificadores (variables)
 */
public class NodoIdentificador extends NodoExpresion {
    private String nombre;
    
    public NodoIdentificador(String nombre, int linea) {
        super(linea);
        this.nombre = nombre;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        return nombre;
    }
    
    @Override
    public String toString() {
        return "Id(" + nombre + ")";
    }
}
