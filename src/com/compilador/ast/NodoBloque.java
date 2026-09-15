package com.compilador.ast;

import java.util.List;

import com.compilador.backend.GeneradorCodigo;

/**
 * Nodo para bloques de sentencias { ... }
 */
public class NodoBloque extends NodoSentencia {
    private List<NodoSentencia> sentencias;
    
    public NodoBloque(List<NodoSentencia> sentencias, int linea) {
        super(linea);
        this.sentencias = sentencias;
    }
    
    public List<NodoSentencia> getSentencias() {
        return sentencias;
    }
    
    /**
     * Verifica si este bloque contiene un return que garantiza salida
     * (es decir, todos los caminos de código devuelven un valor)
     */
    public boolean tieneReturnEnTodosCaminos() {
        if (sentencias == null || sentencias.isEmpty()) {
            return false;
        }
        
        // Verificar la última sentencia
        // Si es un return, hay retorno garantizado
        // Si es un si-sino donde ambas ramas tienen retorno, hay retorno garantizado
        
        for (int i = sentencias.size() - 1; i >= 0; i--) {
            NodoSentencia sent = sentencias.get(i);
            if (sent instanceof NodoDevolver) {
                return true;
            }
            if (sent instanceof NodoSi) {
                NodoSi nodoSi = (NodoSi) sent;
                // Un si tiene retorno en todo camino si:
                // 1. Tiene bloque sino
                // 2. Ambas ramas (si y sino) tienen retorno
                NodoSentencia bloqueIf = nodoSi.getBloqueIf();
                NodoSentencia bloqueElse = nodoSi.getBloqueElse();
                
                if (nodoSi.tieneSino() && 
                    bloqueIf instanceof NodoBloque && 
                    bloqueElse instanceof NodoBloque &&
                    ((NodoBloque) bloqueIf).tieneReturnEnTodosCaminos() &&
                    ((NodoBloque) bloqueElse).tieneReturnEnTodosCaminos()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        for (NodoSentencia sentencia : sentencias) {
            if (sentencia != null) {
                sentencia.generarCodigo(gen);
            }
        }
        return null;
    }
}
