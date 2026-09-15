package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import java.util.List;

/**
 * Nodo para llamadas a funciones como sentencia (sin usar el valor de retorno)
 */
public class NodoLlamadaSentencia extends NodoSentencia {
    private String nombreFuncion;
    private List<NodoExpresion> argumentos;
    
    public NodoLlamadaSentencia(String nombre, List<NodoExpresion> args, int linea) {
        super(linea);
        this.nombreFuncion = nombre;
        this.argumentos = args;
    }
    
    public String getNombreFuncion() {
        return nombreFuncion;
    }
    
    public List<NodoExpresion> getArgumentos() {
        return argumentos;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        // Generar código para evaluar cada argumento
        for (NodoExpresion arg : argumentos) {
            String temp = arg.generarCodigo(gen);
            gen.emitirParametro(temp);
        }
        
        // Generar la llamada
        gen.emitirLlamada(nombreFuncion, argumentos.size());
        
        return "";
    }
    
    @Override
    public String toString() {
        return "LlamadaSentencia(" + nombreFuncion + ", args=" + argumentos.size() + ")";
    }
}
