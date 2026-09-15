package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import com.compilador.semantico.TipoDato;

/**
 * Nodo para declaraciones de variables
 */
public class NodoDeclaracionVariable extends NodoSentencia {
    private TipoDato tipoVariable;
    private String nombre;
    private Integer valorConstante;
    private NodoExpresion valorInicial;
    private boolean esConstante;
    
    public NodoDeclaracionVariable(TipoDato tipo, String nombre, int linea) {
        super(linea);
        this.tipoVariable = tipo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.esConstante = false;
        this.valorConstante = null;
        this.valorInicial = null;
    }
    
    public NodoDeclaracionVariable(TipoDato tipo, String nombre, int linea, Integer valorConstante) {
        super(linea);
        this.tipoVariable = tipo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.esConstante = true;
        this.valorConstante = valorConstante;
        this.valorInicial = null;
    }
    
    public NodoDeclaracionVariable(TipoDato tipo, String nombre, int linea, NodoExpresion valorInicial) {
        super(linea);
        this.tipoVariable = tipo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.esConstante = false;
        this.valorConstante = null;
        this.valorInicial = valorInicial;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public TipoDato getTipoVariable() {
        return tipoVariable;
    }
    
    public boolean esConstante() {
        return esConstante;
    }
    
    public Integer getValorConstante() {
        return valorConstante;
    }
    
    public NodoExpresion getValorInicial() {
        return valorInicial;
    }
    
    public boolean tieneValorInicial() {
        return valorInicial != null;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        // Si es una constante, generar comentario y asignación de constante
        if (esConstante && valorConstante != null) {
            gen.emitir("# Declaracion: CONSTANTE " + tipoVariable + " " + nombre);
            gen.emitirAsignacion(nombre, valorConstante.toString());
        }
        // Si hay un valor inicial para una variable, generar comentario especial sin generar el valor aún
        else if (!esConstante && valorInicial != null) {
            gen.emitir("# Declaracion VARIABLE INICIALIZADA: " + tipoVariable + " " + nombre);
            // Generar la asignación
            String valorExp = valorInicial.generarCodigo(gen);
            gen.emitirAsignacion(nombre, valorExp);
        }
        // Variable sin inicializar
        else {
            gen.emitir("# Declaracion: " + tipoVariable + " " + nombre);
        }
        return null;
    }
}
