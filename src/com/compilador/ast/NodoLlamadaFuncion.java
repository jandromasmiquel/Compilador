package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import java.util.List;

/**
 * Nodo para llamadas a funciones
 */
public class NodoLlamadaFuncion extends NodoExpresion {
    private String nombreFuncion;
    private List<NodoExpresion> argumentos;
    
    public NodoLlamadaFuncion(String nombre, List<NodoExpresion> args, int linea) {
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
        
        // El resultado queda en un temporal
        String resultado = gen.nuevoTemporal();
        gen.emitir(resultado + " = call_result");
        return resultado;
    }
    
    @Override
    public String toString() {
        return "Llamada(" + nombreFuncion + ", args=" + argumentos.size() + ")";
    }
}
