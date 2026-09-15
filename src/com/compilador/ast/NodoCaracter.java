package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import com.compilador.semantico.TipoDato;

/**
 * Nodo para literales de caracter
 */
public class NodoCaracter extends NodoExpresion {
    private String valor;
    
    public NodoCaracter(String valor, int linea) {
        super(linea);
        this.valor = valor;
        this.tipo = TipoDato.CARACTER;
    }
    
    public String getValor() {
        return valor;
    }
    
    /**
     * Obtiene el valor ASCII del caracter
     */
    public int getValorAscii() {
        // Remover las comillas simples
        String limpio = valor.replace("'", "");
        
        // Si está vacío, devolver 0
        if (limpio.isEmpty()) {
            return 0;
        }
        
        // Manejar caracteres escapados
        if (limpio.length() >= 2 && limpio.startsWith("\\")) {
            switch (limpio) {
                case "\\n": return 10;  // Nueva línea
                case "\\t": return 9;   // Tabulador
                case "\\r": return 13;  // Retorno de carro
                case "\\\\": return 92; // Barra invertida
                case "\\'": return 39;  // Comilla simple
                case "\\0": return 0;   // Carácter nulo
                default: 
                    // Otros caracteres escapados, tomar el segundo caracter
                    if (limpio.length() > 1) {
                        return limpio.charAt(1);
                    }
                    return limpio.charAt(0);
            }
        }
        
        return limpio.charAt(0);
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        String temp = gen.nuevoTemporal();
        gen.emitir(temp + " := " + getValorAscii());
        return temp;
    }
    
    @Override
    public String toString() {
        return "Caracter(" + valor + ")";
    }
}
