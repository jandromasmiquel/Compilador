package com.compilador.ast;

import java.util.ArrayList;
import java.util.List;

import com.compilador.backend.GeneradorCodigo;
import com.compilador.semantico.TipoDato;

/**
 * Nodo para declaración e inicialización de arrays (unidimensionales y multidimensionales)
 */
public class NodoDeclaracionArray extends NodoDeclaracionVariable {
    private List<Integer> dimensiones;  // Lista de dimensiones [dim1, dim2, ...]
    private List<NodoExpresion> valores;
    
    // Constructor para arrays unidimensionales (retrocompatibilidad)
    public NodoDeclaracionArray(TipoDato tipoElemento, String nombre, int dimension, 
                                List<NodoExpresion> valores, int linea) {
        super(tipoElemento, nombre, linea);
        this.dimensiones = new ArrayList<>();
        this.dimensiones.add(dimension);
        this.valores = valores;
    }
    
    // Constructor para arrays multidimensionales
    public NodoDeclaracionArray(TipoDato tipoElemento, String nombre, List<Integer> dimensiones, 
                                List<NodoExpresion> valores, int linea) {
        super(tipoElemento, nombre, linea);
        this.dimensiones = dimensiones;
        this.valores = valores;
    }
    
    public int getDimension() {
        // Retorna el tamaño total (producto de todas las dimensiones)
        int total = 1;
        for (Integer dim : dimensiones) {
            total *= dim;
        }
        return total;
    }
    
    public List<Integer> getDimensiones() {
        return dimensiones;
    }
    
    public int getNumDimensiones() {
        return dimensiones.size();
    }
    
    public List<NodoExpresion> getValores() {
        return valores;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        // Declaración del array con sus dimensiones
        StringBuilder dimsStr = new StringBuilder();
        for (Integer dim : dimensiones) {
            dimsStr.append("[").append(dim).append("]");
        }
        gen.emitir("# Declaracion: " + getTipoVariable() + dimsStr.toString() + " " + getNombre());
        
        // Inicializar cada elemento del array (linealizado)
        for (int i = 0; i < valores.size(); i++) {
            String valorTemp = valores.get(i).generarCodigo(gen);
            gen.emitir(getNombre() + "[" + i + "] = " + valorTemp);
        }
        
        return null;
    }
}
