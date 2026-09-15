package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import java.util.List;

/**
 * Nodo raíz del AST - representa todo el programa
 */
public class NodoPrograma extends Nodo {
    private List<Nodo> declaraciones;
    
    public NodoPrograma(List<Nodo> declaraciones) {
        super(0);
        this.declaraciones = declaraciones;
    }
    
    public List<Nodo> getDeclaraciones() {
        return declaraciones;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        gen.emitir("# Programa compilado - Compilador 2025");
        gen.emitir("");
        
        for (Nodo decl : declaraciones) {
            if (decl != null) {
                decl.generarCodigo(gen);
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return "Programa(" + declaraciones.size() + " declaraciones)";
    }
}
