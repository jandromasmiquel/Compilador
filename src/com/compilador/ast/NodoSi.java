package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para sentencias if/si
 */
public class NodoSi extends NodoSentencia {
    private NodoExpresion condicion;
    private NodoSentencia sentenciaVerdadera;
    private NodoSentencia sentenciaFalsa;
    
    public NodoSi(NodoExpresion cond, NodoSentencia siVerdad, NodoSentencia siFalso, int linea) {
        super(linea);
        this.condicion = cond;
        this.sentenciaVerdadera = siVerdad;
        this.sentenciaFalsa = siFalso;
    }
    
    public boolean tieneSino() {
        return sentenciaFalsa != null;
    }
    
    public NodoSentencia getBloqueIf() {
        return sentenciaVerdadera;
    }
    
    public NodoSentencia getBloqueElse() {
        return sentenciaFalsa;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        String tempCond = condicion.generarCodigo(gen);
        String etiqFalso = gen.nuevaEtiqueta();
        String etiqFin = gen.nuevaEtiqueta();
        
        // Si la condición es falsa, saltar a etiqFalso
        gen.emitirSaltoCondicionalFalso(tempCond, etiqFalso);
        
        // Código de la rama verdadera
        if (sentenciaVerdadera != null) {
            sentenciaVerdadera.generarCodigo(gen);
        }
        
        // Saltar al fin (para evitar ejecutar la rama falsa)
        if (sentenciaFalsa != null) {
            gen.emitirSalto(etiqFin);
        }
        
        // Etiqueta de la rama falsa
        gen.emitirEtiqueta(etiqFalso);
        if (sentenciaFalsa != null) {
            sentenciaFalsa.generarCodigo(gen);
        }
        
        // Etiqueta de fin
        if (sentenciaFalsa != null) {
            gen.emitirEtiqueta(etiqFin);
        }
        
        return null;
    }
}
