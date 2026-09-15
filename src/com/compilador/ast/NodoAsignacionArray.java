package com.compilador.ast;

import java.util.ArrayList;
import java.util.List;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para asignación a elemento de array (escritura) - soporta multidimensional
 */
public class NodoAsignacionArray extends NodoSentencia {
    private String nombreArray;
    private List<NodoExpresion> indices;  // Lista de índices para cada dimensión
    private NodoExpresion valor;
    private List<Integer> dimensiones;     // Dimensiones del array (para calcular offset)
    
    // Constructor para arrays unidimensionales (retrocompatibilidad)
    public NodoAsignacionArray(String nombreArray, NodoExpresion indice, 
                               NodoExpresion valor, int linea) {
        super(linea);
        this.nombreArray = nombreArray;
        this.indices = new ArrayList<>();
        this.indices.add(indice);
        this.valor = valor;
        this.dimensiones = new ArrayList<>();
    }
    
    // Constructor para arrays multidimensionales
    public NodoAsignacionArray(String nombreArray, List<NodoExpresion> indices, 
                               NodoExpresion valor, int linea) {
        super(linea);
        this.nombreArray = nombreArray;
        this.indices = indices;
        this.valor = valor;
        this.dimensiones = new ArrayList<>();
    }
    
    public String getNombreArray() {
        return nombreArray;
    }
    
    public NodoExpresion getIndice() {
        return indices.get(0);
    }
    
    public List<NodoExpresion> getIndices() {
        return indices;
    }
    
    public NodoExpresion getValor() {
        return valor;
    }
    
    public void setDimensiones(List<Integer> dims) {
        this.dimensiones = dims;
    }
    
    public List<Integer> getDimensiones() {
        return dimensiones;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        String valorTemp = valor.generarCodigo(gen);
        
        if (indices.size() == 1) {
            // Array unidimensional - comportamiento original
            String indiceTemp = indices.get(0).generarCodigo(gen);
            gen.emitir(nombreArray + "[" + indiceTemp + "] = " + valorTemp);
        } else {
            // Array multidimensional - calcular índice linealizado
            String offsetTemp = gen.nuevoTemporal();
            gen.emitir(offsetTemp + " = 0");
            
            for (int i = 0; i < indices.size(); i++) {
                String indiceTemp = indices.get(i).generarCodigo(gen);
                
                // Calcular el multiplicador para esta dimensión
                int multiplicador = 1;
                for (int j = i + 1; j < dimensiones.size(); j++) {
                    multiplicador *= dimensiones.get(j);
                }
                
                if (multiplicador > 1) {
                    String mulTemp = gen.nuevoTemporal();
                    gen.emitir(mulTemp + " = " + indiceTemp + " * " + multiplicador);
                    String newOffset = gen.nuevoTemporal();
                    gen.emitir(newOffset + " = " + offsetTemp + " + " + mulTemp);
                    offsetTemp = newOffset;
                } else {
                    String newOffset = gen.nuevoTemporal();
                    gen.emitir(newOffset + " = " + offsetTemp + " + " + indiceTemp);
                    offsetTemp = newOffset;
                }
            }
            
            gen.emitir(nombreArray + "[" + offsetTemp + "] = " + valorTemp);
        }
        
        return null;
    }
}
