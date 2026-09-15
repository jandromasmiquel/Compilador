package com.compilador.semantico;

import com.compilador.herramientas.GestorErrores;

/**
 * Comprobador de tipos para el análisis semántico
 * Verifica compatibilidad de tipos en operaciones y asignaciones
 */
public class ComprobadorTipos {
    
    private GestorErrores gestorErrores;
    
    public ComprobadorTipos(GestorErrores gestorErrores) {
        this.gestorErrores = gestorErrores;
    }
    
    /**
     * Comprueba si dos tipos son compatibles para una asignación
     */
    public boolean sonCompatibles(TipoDato tipoIzq, TipoDato tipoDer) {
        if (tipoIzq == TipoDato.ERROR || tipoDer == TipoDato.ERROR) {
            return false;
        }
        return tipoIzq == tipoDer;
    }
    
    /**
     * Obtiene el tipo resultado de una operación aritmética
     */
    public TipoDato tipoOperacionAritmetica(TipoDato tipo1, TipoDato tipo2, String operador, int linea) {
        if (tipo1 == TipoDato.ENTERO && tipo2 == TipoDato.ENTERO) {
            return TipoDato.ENTERO;
        }
        
        gestorErrores.reportarError(linea, 
            "Operación aritmética '" + operador + "' no válida entre " + tipo1 + " y " + tipo2);
        return TipoDato.ERROR;
    }
    
    /**
     * Obtiene el tipo resultado de una operación relacional
     */
    public TipoDato tipoOperacionRelacional(TipoDato tipo1, TipoDato tipo2, String operador, int linea) {
        // Todas las operaciones relacionales permitidas para enteros devuelven booleano
        if (tipo1 == TipoDato.ENTERO && tipo2 == TipoDato.ENTERO) {
            return TipoDato.BOOLEANO;
        }
        
        // Booleanos pueden compararse con == y !=
        if (tipo1 == TipoDato.BOOLEANO && tipo2 == TipoDato.BOOLEANO && 
            (operador.equals("==") || operador.equals("!="))) {
            return TipoDato.BOOLEANO;
        }
        
        // Caracteres pueden compararse con todos los operadores relacionales
        if (tipo1 == TipoDato.CARACTER && tipo2 == TipoDato.CARACTER) {
            return TipoDato.BOOLEANO;
        }
        
        // Solo == y != permitidos para cadenas
        if (tipo1 == TipoDato.CADENA && tipo2 == TipoDato.CADENA && 
            (operador.equals("==") || operador.equals("!="))) {
            return TipoDato.BOOLEANO;
        }
        
        gestorErrores.reportarError(linea,
            "Operación relacional '" + operador + "' no válida entre " + tipo1 + " y " + tipo2);
        return TipoDato.ERROR;
    }
    
    /**
     * Obtiene el tipo resultado de una operación lógica
     */
    public TipoDato tipoOperacionLogica(TipoDato tipo1, TipoDato tipo2, String operador, int linea) {
        // Las operaciones lógicas funcionan con booleanos
        if (tipo1 == TipoDato.BOOLEANO && tipo2 == TipoDato.BOOLEANO) {
            return TipoDato.BOOLEANO;
        }
        
        gestorErrores.reportarError(linea,
            "Operación lógica '" + operador + "' no válida entre " + tipo1 + " y " + tipo2);
        return TipoDato.ERROR;
    }
    
    /**
     * Obtiene el tipo resultado de una operación unaria (negación)
     */
    public TipoDato tipoOperacionUnaria(String operador, TipoDato tipo, int linea) {
        // La negación lógica funciona con booleanos
        if (operador.equals("!") && tipo == TipoDato.BOOLEANO) {
            return TipoDato.BOOLEANO;
        }
        
        gestorErrores.reportarError(linea,
            "Operación unaria '" + operador + "' no válida para tipo " + tipo);
        return TipoDato.ERROR;
    }

    /**
     * Verifica la compatibilidad de una asignación
     */
    public void verificarAsignacion(TipoDato tipoVariable, TipoDato tipoExpresion, int linea) {
        if (!sonCompatibles(tipoVariable, tipoExpresion)) {
            gestorErrores.reportarError(linea,
                "Tipo incompatible en asignación: esperado " + tipoVariable + ", encontrado " + tipoExpresion);
        }
    }
    
    /**
     * Verifica que la condición de una sentencia sea de tipo válido
     */
    public void verificarCondicion(TipoDato tipoCondicion, int linea) {
        // Aceptamos tanto entero como booleano en condiciones
        if (tipoCondicion != TipoDato.ENTERO && tipoCondicion != TipoDato.BOOLEANO && tipoCondicion != TipoDato.ERROR) {
            gestorErrores.reportarError(linea,
                "La condición debe ser de tipo entero o booleano, encontrado: " + tipoCondicion);
        }
    }
    
    /**
     * Verifica que el tipo de retorno coincida con el tipo de la función
     */
    public void verificarRetorno(TipoDato tipoFuncion, TipoDato tipoRetorno, int linea) {
        if (!sonCompatibles(tipoFuncion, tipoRetorno)) {
            gestorErrores.reportarError(linea,
                "Tipo de retorno incompatible: esperado " + tipoFuncion + ", encontrado " + tipoRetorno);
        }
    }
    
    /**
     * Verifica los argumentos de una llamada a función
     */
    public void verificarArgumentosFuncion(String nombreFuncion, java.util.List<TipoDato> tiposArgumentos, 
                                           java.util.List<TipoDato> tiposEsperados, int linea) {
        // Verificar número de argumentos
        if (tiposArgumentos.size() != tiposEsperados.size()) {
            gestorErrores.reportarError(linea,
                "Número incorrecto de argumentos en '" + nombreFuncion + "': esperados " + 
                tiposEsperados.size() + ", dados " + tiposArgumentos.size());
            return;
        }
        
        // Verificar tipo de cada argumento
        for (int i = 0; i < tiposArgumentos.size(); i++) {
            TipoDato tipoArg = tiposArgumentos.get(i);
            TipoDato tipoEsperado = tiposEsperados.get(i);
            
            if (!sonCompatibles(tipoEsperado, tipoArg)) {
                gestorErrores.reportarError(linea,
                    "Tipo incorrecto en argumento " + (i + 1) + " de '" + nombreFuncion + 
                    "': esperado " + tipoEsperado + ", dado " + tipoArg);
            }
        }
    }
    
    /**
     * Verifica que una función devuelva un valor si no es void
     */
    public void verificarReturnFuncion(TipoDato tipoFuncion, String nombreFuncion, boolean tieneReturn, int linea) {
        if (!tieneReturn && tipoFuncion != TipoDato.VOID) {
            gestorErrores.reportarError(linea,
                "Función '" + nombreFuncion + "' de tipo " + tipoFuncion + 
                " debe tener un return en todas las rutas de código");
        }
    }
}

