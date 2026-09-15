package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para sentencias while/mientras
 */
public class NodoMientras extends NodoSentencia {
    private NodoExpresion condicion;
    private NodoSentencia cuerpo;
    
    public NodoMientras(NodoExpresion cond, NodoSentencia cuerpo, int linea) {
        super(linea);
        this.condicion = cond;
        this.cuerpo = cuerpo;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        String etiqInicio = gen.nuevaEtiqueta();
        String etiqFin = gen.nuevaEtiqueta();
        
        // Etiqueta de inicio del bucle
        gen.emitirEtiqueta(etiqInicio);
        
        // Evaluar condición
        String tempCond = condicion.generarCodigo(gen);
        
        // Si es falsa, salir del bucle
        gen.emitirSaltoCondicionalFalso(tempCond, etiqFin);
        
        // Cuerpo del bucle
        if (cuerpo != null) {
            cuerpo.generarCodigo(gen);
        }
        
        // Volver al inicio
        gen.emitirSalto(etiqInicio);
        
        // Etiqueta de fin
        gen.emitirEtiqueta(etiqFin);
        
        return null;
    }
}
