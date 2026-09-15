package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para sentencias for/para
 * Estructura: para (inicialización; condición; incremento) sentencia
 */
public class NodoParaBucle extends NodoSentencia {
    private NodoSentencia inicializacion;
    private NodoExpresion condicion;
    private NodoSentencia incremento;
    private NodoSentencia cuerpo;
    
    public NodoParaBucle(NodoSentencia init, NodoExpresion cond, NodoSentencia incr, NodoSentencia cuerpo, int linea) {
        super(linea);
        this.inicializacion = init;
        this.condicion = cond;
        this.incremento = incr;
        this.cuerpo = cuerpo;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        // Ejecutar inicialización
        if (inicializacion != null) {
            inicializacion.generarCodigo(gen);
        }
        
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
        
        // Ejecutar incremento
        if (incremento != null) {
            incremento.generarCodigo(gen);
        }
        
        // Volver al inicio
        gen.emitirSalto(etiqInicio);
        
        // Etiqueta de fin
        gen.emitirEtiqueta(etiqFin);
        
        return null;
    }
}
