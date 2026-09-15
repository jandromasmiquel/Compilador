package com.compilador.ast;

import java.util.ArrayList;
import java.util.List;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para acceso a elemento de array (lectura) - soporta multidimensional
 */
public class NodoAccesoArray extends NodoExpresion {
    private String nombreArray;
    private List<NodoExpresion> indices;  // Lista de índices para cada dimensión
    private List<Integer> dimensiones;     // Dimensiones del array (para calcular offset)
    
    // Constructor para arrays unidimensionales (retrocompatibilidad)
    public NodoAccesoArray(String nombreArray, NodoExpresion indice, int linea) {
        super(linea);
        this.nombreArray = nombreArray;
        this.indices = new ArrayList<>();
        this.indices.add(indice);
        this.dimensiones = new ArrayList<>();
    }
    
    // Constructor para arrays multidimensionales
    public NodoAccesoArray(String nombreArray, List<NodoExpresion> indices, int linea) {
        super(linea);
        this.nombreArray = nombreArray;
        this.indices = indices;
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
    
    public void setDimensiones(List<Integer> dims) {
        this.dimensiones = dims;
    }
    
    public List<Integer> getDimensiones() {
        return dimensiones;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        if (indices.size() == 1) {
            // Array unidimensional - comportamiento original
            String indiceTemp = indices.get(0).generarCodigo(gen);
            String temp = gen.nuevoTemporal();
            gen.emitir(temp + " = " + nombreArray + "[" + indiceTemp + "]");
            return temp;
        } else {
            // Array multidimensional - calcular índice linealizado
            // Para un array[d1][d2], el índice lineal es: i1 * d2 + i2
            // Para un array[d1][d2][d3], es: i1 * d2 * d3 + i2 * d3 + i3
            
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
            
            String temp = gen.nuevoTemporal();
            gen.emitir(temp + " = " + nombreArray + "[" + offsetTemp + "]");
            return temp;
        }
    }
}
