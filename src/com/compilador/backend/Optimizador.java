package com.compilador.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Optimizador de código intermedio
 * Implementa optimizaciones básicas: eliminación de código muerto,
 * propagación de constantes, y eliminación de subexpresiones comunes
 */
public class Optimizador {
    
    private List<String> codigo;
    
    public Optimizador(List<String> codigo) {
        this.codigo = new ArrayList<>(codigo);
    }
    
    /**
     * Aplica todas las optimizaciones disponibles
     */
    public List<String> optimizar() {
        eliminarCodigoMuerto();
        propagarConstantes();
        eliminarSubexpresionesComunes();
        return codigo;
    }
    
    /**
     * Elimina código muerto (instrucciones que nunca se ejecutan)
     */
    private void eliminarCodigoMuerto() {
        Set<String> etiquetasReferenciadas = new HashSet<>();
        
        // Primera pasada: identificar etiquetas referenciadas
        for (String instruccion : codigo) {
            if (instruccion.contains("goto") || instruccion.contains("if")) {
                String[] partes = instruccion.split("\\s+");
                for (String parte : partes) {
                    if (parte.startsWith("L")) {
                        etiquetasReferenciadas.add(parte.replace(":", ""));
                    }
                }
            }
        }
        
        // Segunda pasada: eliminar código después de goto hasta la siguiente etiqueta
        List<String> codigoOptimizado = new ArrayList<>();
        boolean despuesDeGoto = false;
        
        for (String instruccion : codigo) {
            if (instruccion.trim().startsWith("goto")) {
                codigoOptimizado.add(instruccion);
                despuesDeGoto = true;
            } else if (instruccion.contains(":")) {
                despuesDeGoto = false;
                codigoOptimizado.add(instruccion);
            } else if (!despuesDeGoto) {
                codigoOptimizado.add(instruccion);
            }
        }
        
        codigo = codigoOptimizado;
    }
    
    /**
     * Propaga constantes a través del código
     */
    private void propagarConstantes() {
        Map<String, String> constantes = new HashMap<>();
        Map<String, String> constantesGlobales = new HashMap<>();
        Set<String> variablesInicializadas = new HashSet<>(); // Variables que NO son constantes
        List<String> codigoOptimizado = new ArrayList<>();
        boolean dentroFuncion = false;
        String ultimoComentario = "";
        
        for (String instruccion : codigo) {
            // Guardar comentarios para detectar variables inicializadas
            if (instruccion.trim().startsWith("#")) {
                ultimoComentario = instruccion.trim();
            }
            
            // Detectar inicio de función
            if (instruccion.trim().endsWith(":") && !instruccion.startsWith("#") 
                && !instruccion.trim().matches("L\\d+:")) {
                dentroFuncion = true;
                // Limpiar constantes locales pero mantener las globales
                constantes.clear();
                constantes.putAll(constantesGlobales);
                codigoOptimizado.add(instruccion);
                ultimoComentario = "";
                continue;
            }
            
            // Limpiar constantes al encontrar una etiqueta de bucle
            if (instruccion.trim().matches("L\\d+:")) {
                // Limpiar constantes locales pero mantener las globales
                constantes.clear();
                constantes.putAll(constantesGlobales);
                codigoOptimizado.add(instruccion);
                ultimoComentario = "";
                continue;
            }
            
            if (instruccion.contains("=") && !instruccion.contains(":") && !instruccion.startsWith("#")) {
                // Detectar si es una asignación simple (no comparación)
                // Buscar el primer = que no esté precedido ni seguido por <, >, !, =
                String[] partes = instruccion.split("(?<![<>!=])=(?![=])");
                if (partes.length == 2) {
                    String izq = partes[0].trim();
                    String der = partes[1].trim();
                    
                    // Detectar si la variable fue declarada como VARIABLE INICIALIZADA
                    boolean esVariableInicializada = ultimoComentario.contains("VARIABLE INICIALIZADA");
                    if (esVariableInicializada && !dentroFuncion) {
                        variablesInicializadas.add(izq);
                    }
                    
                    // Si la variable ya existe en constantes, invalidarla
                    if (constantes.containsKey(izq)) {
                        constantes.remove(izq);
                    }
                    
                    // Si es una asignación de constante Y NO es variable inicializada
                    if (esNumero(der) && !variablesInicializadas.contains(izq)) {
                        // Verificar si es una constante real (tiene comentario CONSTANTE)
                        boolean esConstanteReal = ultimoComentario.contains("CONSTANTE");
                        
                        constantes.put(izq, der);
                        // Si es antes de cualquier función Y es constante real, es una constante global
                        if (!dentroFuncion && esConstanteReal) {
                            constantesGlobales.put(izq, der);
                        }
                        codigoOptimizado.add(instruccion);
                    } else {
                        // Reemplazar variables por constantes conocidas
                        for (Map.Entry<String, String> entry : constantes.entrySet()) {
                            // Usar replaceAll con límites de palabra para evitar reemplazos parciales
                            der = der.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
                        }
                        codigoOptimizado.add(izq + " = " + der);
                    }
                    ultimoComentario = "";
                } else {
                    codigoOptimizado.add(instruccion);
                    ultimoComentario = "";
                }
            } else {
                // Reemplazar constantes en otras instrucciones (if, goto, return, etc.)
                String instruccionOptimizada = instruccion;
                for (Map.Entry<String, String> entry : constantes.entrySet()) {
                    // Reemplazar solo si la variable está rodeada de espacios, operadores o al final
                    instruccionOptimizada = instruccionOptimizada.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
                }
                codigoOptimizado.add(instruccionOptimizada);
                
                // Si hay una llamada a función, invalidar las constantes de variables globales
                // porque las funciones pueden modificar variables globales
                if (instruccion.contains("call")) {
                    // Mantener solo las constantes globales declaradas al inicio
                    constantes.keySet().removeIf(k -> !constantesGlobales.containsKey(k));
                    constantes.putAll(constantesGlobales); // Restaurar constantes globales
                }
                
                // Limpiar ultimoComentario si no es un comentario
                if (!instruccion.trim().startsWith("#")) {
                    ultimoComentario = "";
                }
            }
        }
        
        codigo = codigoOptimizado;
    }
    
    /**
     * Elimina subexpresiones comunes
     */
    private void eliminarSubexpresionesComunes() {
        Map<String, String> expresiones = new HashMap<>();
        List<String> codigoOptimizado = new ArrayList<>();
        
        for (String instruccion : codigo) {
            if (instruccion.contains("=") && !instruccion.contains(":") && !instruccion.startsWith("#")) {
                // Detectar si es una asignación simple (no comparación)
                String[] partes = instruccion.split("(?<![<>!=])=(?![=])");
                if (partes.length == 2) {
                    String izq = partes[0].trim();
                    String der = partes[1].trim();
                    
                    // Solo eliminar subexpresiones comunes si NO es una asignación literal de constante
                    // Las asignaciones como "variable = 0" no deben compartirse
                    boolean esAsignacionLiteral = esNumero(der) || der.startsWith("\"");
                    
                    // Si la expresión ya fue calculada Y no es una asignación literal
                    if (expresiones.containsKey(der) && !esAsignacionLiteral) {
                        codigoOptimizado.add(izq + " = " + expresiones.get(der));
                    } else {
                        // Solo guardar expresiones complejas, no literales
                        if (!esAsignacionLiteral) {
                            expresiones.put(der, izq);
                        }
                        codigoOptimizado.add(instruccion);
                    }
                } else {
                    codigoOptimizado.add(instruccion);
                }
            } else {
                codigoOptimizado.add(instruccion);
                // Limpiar el mapa en puntos de control
                if (instruccion.contains(":") || instruccion.contains("call")) {
                    expresiones.clear();
                }
            }
        }
        
        codigo = codigoOptimizado;
    }
    
    /**
     * Verifica si una cadena es un número
     */
    private boolean esNumero(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Imprime estadísticas de optimización
     */
    public void imprimirEstadisticas(int tamanioOriginal) {
        int tamanioOptimizado = codigo.size();
        int instruccionesEliminadas = tamanioOriginal - tamanioOptimizado;
        double porcentaje = (instruccionesEliminadas * 100.0) / tamanioOriginal;
        
        System.out.println("\n=== ESTADÍSTICAS DE OPTIMIZACIÓN ===");
        System.out.println("Instrucciones originales: " + tamanioOriginal);
        System.out.println("Instrucciones optimizadas: " + tamanioOptimizado);
        System.out.println("Instrucciones eliminadas: " + instruccionesEliminadas);
        System.out.printf("Reducción: %.2f%%%n", porcentaje);
    }
}
