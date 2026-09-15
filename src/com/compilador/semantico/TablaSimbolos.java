package com.compilador.semantico;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * Tabla de símbolos para gestionar variables y funciones
 * Soporta ámbitos anidados mediante una pila de tablas hash
 */
public class TablaSimbolos {
    
    // Clase interna para representar un símbolo
    public static class Simbolo {
        private String nombre;
        private TipoDato tipo;
        private String ambito;
        private int linea;
        private boolean esParametro;
        private boolean esConstante;
        private Object valorConstante; // Valor de la constante si aplica
        private List<TipoDato> parametros; // Para funciones
        private boolean esArray; // Indica si es un array
        private int dimensionArray; // Tamaño del array si es array (1D) o total elementos
        private List<Integer> dimensiones; // Lista de dimensiones para arrays multidimensionales
        private boolean esFuncion; // Indica si es una función


        public Simbolo(String nombre, TipoDato tipo, String ambito, int linea) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.ambito = ambito;
            this.linea = linea;
            this.esParametro = false;
            this.esConstante = false;
            this.valorConstante = null;
            this.parametros = new ArrayList<>();
            this.esArray = false;
            this.dimensionArray = 0;
            this.dimensiones = new ArrayList<>();
            this.esFuncion = false;
        }
        
        public String getNombre() { return nombre; }
        public TipoDato getTipo() { return tipo; }
        public String getAmbito() { return ambito; }
        public int getLinea() { return linea; }
        public boolean esParametro() { return esParametro; }
        public void setEsParametro(boolean valor) { esParametro = valor; }
        public List<TipoDato> getParametros() { return parametros; }
        public boolean esConstante() { return esConstante; }
        public void setEsConstante(boolean valor) { esConstante = valor; }
        public Object getValorConstante() { return valorConstante; }
        public void setValorConstante(Object valor) { valorConstante = valor; }
        public boolean esArray() { return esArray; }
        public void setEsArray(boolean valor) { esArray = valor; }
        public int getDimensionArray() { return dimensionArray; }
        public void setDimensionArray(int dimension) { dimensionArray = dimension; }
        public List<Integer> getDimensiones() { return dimensiones; }
        public void setDimensiones(List<Integer> dims) { this.dimensiones = dims; }
        public int getNumDimensiones() { return dimensiones.size(); }
        public boolean esArrayMultidimensional() { return esArray && dimensiones.size() > 1; }
        public boolean esFuncion() { return esFuncion; }
        public void setEsFuncion(boolean valor) { esFuncion = valor; }
        
        public List<TipoDato> getTiposParametros() { 
            return parametros != null ? parametros : new ArrayList<>(); 
        }
        
        public void setTiposParametros(List<TipoDato> tipos) { 
            this.parametros = tipos; 
        }
        
        @Override
        public String toString() {
            if (esArray) {
                StringBuilder dims = new StringBuilder();
                for (Integer d : dimensiones) {
                    dims.append("[").append(d).append("]");
                }
                return String.format("%-20s %-15s %-30s %d %s", nombre, tipo + dims.toString(), ambito, linea, dims.toString());
            }
            return String.format("%-20s %-15s %-30s %d", nombre, tipo, ambito, linea);
        }
    }

    // Pila de ámbitos (cada nivel es un HashMap)
    private Stack<HashMap<String, Simbolo>> pilaAmbitos;
    private Stack<String> pilaNombresAmbitos;
    private String ambitoActual;
    private List<Simbolo> simbolosGuardados; // Guardar todos los símbolos
    
    public TablaSimbolos() {
        pilaAmbitos = new Stack<>();
        pilaNombresAmbitos = new Stack<>();
        pilaAmbitos.push(new HashMap<>()); // Ámbito global
        pilaNombresAmbitos.push("global");
        ambitoActual = "global";
        simbolosGuardados = new ArrayList<>();
    }
    
    /**
     * Entra en un nuevo ámbito
     */
    public void entrarAmbito(String nombreAmbito) {
        pilaAmbitos.push(new HashMap<>());
        pilaNombresAmbitos.push(nombreAmbito);
        ambitoActual = nombreAmbito;
    }
    
    /**
     * Sale del ámbito actual
     */
    public void salirAmbito() {
        if (pilaAmbitos.size() > 1) {
            pilaAmbitos.pop();
            pilaNombresAmbitos.pop();
            ambitoActual = pilaNombresAmbitos.peek(); // Restaura ámbito previo
        }
    }    
    /**
     * Obtiene todos los símbolos registrados
     */
    public List<Simbolo> obtenerTodosSimbolo() {
        return new ArrayList<>(simbolosGuardados);
    }    
    

    /**
     * Inserta un símbolo en el ámbito actual
     */
    public boolean insertar(String nombre, TipoDato tipo, int linea) {
        HashMap<String, Simbolo> ambitoActualMap = pilaAmbitos.peek();
        
        if (ambitoActualMap.containsKey(nombre)) {
            return false; // Ya existe en este ámbito
        }
        
        // Usar la jerarquía completa como nombre de ámbito
        String nombreAmbito = getJerarquiaAmbitos();
        Simbolo simbolo = new Simbolo(nombre, tipo, nombreAmbito, linea);
        ambitoActualMap.put(nombre, simbolo);
        simbolosGuardados.add(simbolo); // Guardar en lista permanente
        return true;
    }
    
    /**
     * Obtiene el ámbito (función) actual
     */
    public String getFuncionActual() {
        // La pila tiene: [0]=global, [1]=función, [2..n]=bloques anidados
        // Queremos devolver el nombre de la función (índice 1)
        if (pilaNombresAmbitos.size() > 1) {
            // Obtener el elemento en índice 1 (la función)
            return pilaNombresAmbitos.get(1);
        }
        return "global";
    }
    
    /**
     * Obtiene la jerarquía completa de ámbitos actual (excluyendo global)
     * Por ejemplo: "main_bloque1_bloque2"
     */
    public String getJerarquiaAmbitos() {
        StringBuilder jerarquia = new StringBuilder();
        for (int i = 1; i < pilaNombresAmbitos.size(); i++) {
            if (jerarquia.length() > 0) {
                jerarquia.append("_");
            }
            jerarquia.append(pilaNombresAmbitos.get(i));
        }
        return jerarquia.length() > 0 ? jerarquia.toString() : "global";
    }
    
    /**
     * Busca un símbolo en el ámbito global
     */
    public Simbolo buscarGlobal(String nombre) {
        HashMap<String, Simbolo> ambitoGlobal = pilaAmbitos.get(0);
        return ambitoGlobal.get(nombre);
    }
    
    /**
     * Busca un símbolo en todos los ámbitos (desde el más interno)
     */
    public Simbolo buscar(String nombre) {
        // Buscar desde el ámbito más interno al más externo
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            HashMap<String, Simbolo> ambito = pilaAmbitos.get(i);
            if (ambito.containsKey(nombre)) {
                return ambito.get(nombre);
            }
        }
        return null;
    }
    
    /**
     * Busca un símbolo solo en el ámbito actual
     */
    public Simbolo buscarEnAmbitoActual(String nombre) {
        HashMap<String, Simbolo> ambitoActualMap = pilaAmbitos.peek();
        return ambitoActualMap.get(nombre);
    }
    
    /**
     * Inserta una constante en el ámbito actual
     */
    public boolean insertarConstante(String nombre, TipoDato tipo, int linea, Object valorConstante) {
        HashMap<String, Simbolo> ambitoActualMap = pilaAmbitos.peek();
        
        if (ambitoActualMap.containsKey(nombre)) {
            return false; // Ya existe en este ámbito
        }
        
        // Usar la jerarquía completa como nombre de ámbito
        String nombreAmbito = getJerarquiaAmbitos();
        Simbolo simbolo = new Simbolo(nombre, tipo, nombreAmbito, linea);
        simbolo.setEsConstante(true);
        simbolo.setValorConstante(valorConstante);
        ambitoActualMap.put(nombre, simbolo);
        simbolosGuardados.add(simbolo);
        return true;
    }
    
    /**
     * Inserta un array en el ámbito actual (unidimensional - retrocompatibilidad)
     */
    public boolean insertarArray(String nombre, TipoDato tipoElemento, int dimension, int linea) {
        List<Integer> dims = new ArrayList<>();
        dims.add(dimension);
        return insertarArrayMultidimensional(nombre, tipoElemento, dims, linea);
    }
    
    /**
     * Inserta un array multidimensional en el ámbito actual
     */
    public boolean insertarArrayMultidimensional(String nombre, TipoDato tipoElemento, List<Integer> dimensiones, int linea) {
        HashMap<String, Simbolo> ambitoActualMap = pilaAmbitos.peek();
        
        if (ambitoActualMap.containsKey(nombre)) {
            return false; // Ya existe en este ámbito
        }
        
        // Usar la jerarquía completa como nombre de ámbito
        String nombreAmbito = getJerarquiaAmbitos();
        Simbolo simbolo = new Simbolo(nombre, tipoElemento, nombreAmbito, linea);
        simbolo.setEsArray(true);
        
        // Calcular total de elementos
        int total = 1;
        for (Integer dim : dimensiones) {
            total *= dim;
        }
        simbolo.setDimensionArray(total);
        simbolo.setDimensiones(dimensiones);
        
        ambitoActualMap.put(nombre, simbolo);
        simbolosGuardados.add(simbolo);
        return true;
    }
    
    /**
     * Volca la tabla de símbolos a un archivo
     */
    public void volcarAArchivo(String rutaArchivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            writer.println("===============================================================================");
            writer.println("                           TABLA DE SÍMBOLOS");
            writer.println("===============================================================================");
            writer.println();
            writer.printf("%-20s %-15s %-30s %s%n", "NOMBRE", "TIPO", "ÁMBITO", "LÍNEA");
            writer.println("-------------------------------------------------------------------------------");
            
            // Recorrer todos los símbolos de todos los ámbitos
            List<Simbolo> todosSimbols = obtenerTodosSimbolo();
            for (Simbolo simbolo : todosSimbols) {
                writer.println(simbolo);
            }
            
            writer.println("===============================================================================");
        }
    }
        
    /**
     * Volca la tabla de variables a un archivo (solo variables, sin funciones)
     */
    public void volcarTablaVariables(String rutaArchivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            writer.println("=====================================================================================");
            writer.println("                            TABLA DE VARIABLES");
            writer.println("=====================================================================================");
            writer.println();
            writer.println("Esta tabla contiene todas las variables y parámetros");
            writer.println("declarados en el programa, útil para verificar el código");
            writer.println("intermedio de tres direcciones.");
            writer.println();
            writer.printf("%-20s %-15s %-30s %-10s %s%n", "NOMBRE", "TIPO", "ÁMBITO", "PARAM", "LÍNEA");
            writer.println("-------------------------------------------------------------------------------------");
            
            List<Simbolo> todosSimbols = obtenerTodosSimbolo();
            int contadorVariables = 0;
            
            for (Simbolo simbolo : todosSimbols) {
                // Solo incluir variables (no funciones)
                // Las funciones tienen ámbito "global" y no son parámetros
                if (!simbolo.getAmbito().equals("global") || simbolo.esParametro()) {
                    String esParam = simbolo.esParametro() ? "Sí" : "No";
                    writer.printf("%-20s %-15s %-30s %-10s %d%n", 
                        simbolo.getNombre(), 
                        simbolo.getTipo(), 
                        simbolo.getAmbito(),
                        esParam,
                        simbolo.getLinea());
                    contadorVariables++;
                } else if (simbolo.getAmbito().equals("global") && !esUnaPosibleFuncion(simbolo)) {
                    // Variables globales (si las hay)
                    String esParam = simbolo.esParametro() ? "Sí" : "No";
                    writer.printf("%-20s %-15s %-30s %-10s %d%n", 
                        simbolo.getNombre(), 
                        simbolo.getTipo(), 
                        simbolo.getAmbito(),
                        esParam,
                        simbolo.getLinea());
                    contadorVariables++;
                }
            }
            
            writer.println("-------------------------------------------------------------------------------------");
            writer.println("Total de variables: " + contadorVariables);
            writer.println("=====================================================================================");
        }
    }
    
    /**
     * Volca la tabla de procedimientos/funciones a un archivo
     */
    public void volcarTablaProcedimientos(String rutaArchivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            writer.println("=====================================================================================");
            writer.println("                      TABLA DE PROCEDIMIENTOS/FUNCIONES");
            writer.println("=====================================================================================");
            writer.println();
            writer.println("Esta tabla contiene todas las funciones/procedimientos");
            writer.println("declarados en el programa.");
            writer.println();
            writer.printf("%-20s %-15s %-30s %s%n", "NOMBRE", "TIPO RETORNO", "ÁMBITO", "LÍNEA");
            writer.println("-------------------------------------------------------------------------------------");
            
            List<Simbolo> todosSimbols = obtenerTodosSimbolo();
            int contadorFunciones = 0;
            
            for (Simbolo simbolo : todosSimbols) {
                // Solo incluir funciones (ámbito global y no son parámetros)
                if (simbolo.getAmbito().equals("global") && esUnaPosibleFuncion(simbolo)) {
                    writer.printf("%-20s %-15s %-30s %d%n", 
                        simbolo.getNombre(), 
                        simbolo.getTipo(), 
                        simbolo.getAmbito(),
                        simbolo.getLinea());
                    contadorFunciones++;
                }
            }
            
            writer.println("-------------------------------------------------------------------------------------");
            writer.println("Total de funciones: " + contadorFunciones);
            writer.println("=====================================================================================");
        }
    }
    
    /**
     * Verifica si un símbolo es una función usando el flag explícito
     */
    private boolean esUnaPosibleFuncion(Simbolo simbolo) {
        if (simbolo == null) {
            return false;
        }
        return simbolo.esFuncion();
    }
}