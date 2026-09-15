package com.compilador.backend;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generador de código ensamblador MIPS
 * Traduce el código intermedio a instrucciones MIPS
 */
public class GeneradorEnsamblador {
    
    private List<String> codigoIntermedio;
    private List<String> codigoEnsamblador;
    private Map<String, Integer> mapaVariables;
    private Map<String, String> cadenasLiterales;
    private Map<String, String> variablesCadena; // Mapea variables a sus etiquetas de cadena
    private Map<String, Integer> parametrosActuales; // Parámetros de la función en curso
    private Map<String, Integer> tamañosArrays; // Mapea nombres de arrays a sus tamaños
    private Map<String, String> variablesGlobales; // Variables globales no constantes
    private Map<String, String> valoresInicialesVariables; // Valores iniciales de variables
    private List<String> inicializacionesArrayGlobal; // Instrucciones de inicialización de arrays globales
    private int offsetVariables;
    private String funcionActual;
    private List<String> parametrosPendientes;
    private int indiceActual; // Índice de instrucción actual durante la traducción
    
    public GeneradorEnsamblador(List<String> codigoIntermedio) {
        this.codigoIntermedio = codigoIntermedio;
        this.codigoEnsamblador = new ArrayList<>();
        this.mapaVariables = new HashMap<>();
        this.cadenasLiterales = new HashMap<>();
        this.variablesCadena = new HashMap<>();
        this.parametrosActuales = new HashMap<>();
        this.tamañosArrays = new HashMap<>();
        this.variablesGlobales = new HashMap<>();
        this.valoresInicialesVariables = new HashMap<>();
        this.inicializacionesArrayGlobal = new ArrayList<>();
        this.offsetVariables = 0;
        this.funcionActual = "";
        this.parametrosPendientes = new ArrayList<>();
        this.indiceActual = 0;
    }
    
    public void setCadenasLiterales(Map<String, String> cadenas) {
        this.cadenasLiterales = cadenas;
    }
    
    /**
     * Genera código ensamblador MIPS
     */
    public void generar() {
        // Prepass 1: Detectar constantes globales (asignaciones antes de funciones, excepto variables inicializadas)
        Map<String, Boolean> constantesGlobales = new HashMap<>();
        Map<String, Boolean> variablesInicializadas = new HashMap<>();
        boolean dentroFuncion = false;
        
        for (int i = 0; i < codigoIntermedio.size(); i++) {
            String linea = codigoIntermedio.get(i).trim();
            
            if (linea.endsWith(":") && !linea.startsWith("#") && !linea.matches("L\\d+:")) {
                dentroFuncion = true;
            }
            
            // Detectar variables inicializadas (comentario especial antes de asignación)
            if (!dentroFuncion && linea.startsWith("# Declaracion VARIABLE INICIALIZADA:")) {
                // La siguiente línea no es comentario debe ser la asignación
                if (i + 1 < codigoIntermedio.size()) {
                    String nextLinea = codigoIntermedio.get(i + 1).trim();
                    if (nextLinea.contains("=") && !nextLinea.startsWith("#")) {
                        String[] partes = nextLinea.split("(?<![<>!=])=(?![=])");
                        if (partes.length == 2) {
                            String variable = partes[0].trim();
                            variablesInicializadas.put(variable, true);
                        }
                    }
                }
            }
            
            // Detectar asignaciones de constantes globales (NO si es variable inicializada)
            if (!dentroFuncion && linea.contains("=") && !linea.contains(":") && !linea.startsWith("#")) {
                String[] partes = linea.split("(?<![<>!=])=(?![=])");
                if (partes.length == 2) {
                    String variable = partes[0].trim();
                    // Solo marcar como constante si NO es variable inicializada
                    if (!variablesInicializadas.containsKey(variable)) {
                        constantesGlobales.put(variable, true);
                    }
                }
            }
        }
        
        // Prepass 2: detectar variables globales (antes de cualquier función) y arrays
        dentroFuncion = false;
        for (int idx = 0; idx < codigoIntermedio.size(); idx++) {
            String linea = codigoIntermedio.get(idx).trim();
            
            // Detectar inicio de función
            if (linea.endsWith(":") && !linea.startsWith("#") && !linea.matches("L\\d+:")) {
                dentroFuncion = true;
            }
            
            // Detectar declaraciones de variables globales simples (no constantes)
            if (!dentroFuncion && linea.startsWith("# Declaracion:") && !linea.contains("[")) {
                String[] partes = linea.substring(14).trim().split("\\s+");
                if (partes.length >= 2) {
                    String tipo = partes[0];
                    String nombre = partes[1];
                    // Solo agregar si NO es una constante y el nombre no es un número
                    if (!constantesGlobales.containsKey(nombre) && !esNumero(nombre)) {
                        variablesGlobales.put(nombre, tipo);
                    }
                }
            }
            
            // Detectar declaraciones de variables inicializadas globales
            if (!dentroFuncion && linea.startsWith("# Declaracion VARIABLE INICIALIZADA:") && !linea.contains("[")) {
                // Formato: # Declaracion VARIABLE INICIALIZADA: tipo nombre
                String contenido = linea.substring(37).trim(); // Remueve el prefijo
                
                String[] partes = contenido.split("\\s+");
                if (partes.length >= 2) {
                    String tipo = partes[0];
                    String nombre = partes[1];
                    if (!esNumero(nombre)) {
                        variablesGlobales.put(nombre, tipo);
                        
                        // Buscar la asignación en la siguiente línea para extraer el valor
                        if (idx + 1 < codigoIntermedio.size()) {
                            String siguienteLinea = codigoIntermedio.get(idx + 1).trim();
                            if (!siguienteLinea.startsWith("#") && siguienteLinea.contains("=")) {
                                String[] partsAsign = siguienteLinea.split("(?<![<>!=])=(?![=])");
                                if (partsAsign.length == 2) {
                                    String varAsign = partsAsign[0].trim();
                                    if (varAsign.equals(nombre)) {
                                        String valor = partsAsign[1].trim();
                                        valoresInicialesVariables.put(nombre, valor);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Detectar arrays globales
            if (linea.startsWith("# Declaracion:") && linea.contains("[") && linea.contains("]")) {
                int posCorcheteAb = linea.indexOf("[");
                int posCorcheteC = linea.indexOf("]");
                
                if (posCorcheteAb != -1 && posCorcheteC != -1) {
                    String tamanosStr = linea.substring(posCorcheteAb + 1, posCorcheteC).trim();
                    String resto = linea.substring(posCorcheteC + 1).trim();
                    String[] partes = resto.split("\\s+");
                    
                    if (partes.length >= 1) {
                        String nombreArray = partes[0];
                        try {
                            int tamaño = Integer.parseInt(tamanosStr);
                            tamañosArrays.put(nombreArray, tamaño);
                        } catch (NumberFormatException e) {
                            // Ignorar
                        }
                    }
                }
            }
        }
        
        generarPrologo();
        for (int i = 0; i < codigoIntermedio.size(); i++) {
            indiceActual = i;
            traducirInstruccion(codigoIntermedio.get(i));
        }
        generarEpilogo();
    }
    
    /**
     * Genera el prólogo del programa
     */
    private void generarPrologo() {
        emitir("# Código generado por Compilador 2025");
        emitir(".data");
        emitir("newline: .asciiz \"\\n\"");
        emitir("buffer: .space 256");  // Buffer para leer cadenas
        
        // Generar todas las cadenas literales
        for (Map.Entry<String, String> entrada : cadenasLiterales.entrySet()) {
            String cadena = entrada.getKey();
            String etiqueta = entrada.getValue();
            emitir(etiqueta + ": .asciiz \"" + cadena + "\"");
        }
        
        // Generar variables globales
        for (Map.Entry<String, String> entrada : variablesGlobales.entrySet()) {
            String nombre = entrada.getKey();
            String valorInicial = valoresInicialesVariables.getOrDefault(nombre, "0");
            emitir(nombre + ": .word " + valorInicial);
        }
        
        emitir();
        emitir(".text");
        emitir(".globl main");
        emitir();
        emitir("    j main                # Saltar a main");
        emitir();
    }
    
    /**
     * Genera el epílogo del programa
     */
    private void generarEpilogo() {
        // No se necesita epílogo, cada función maneja su retorno
    }
    
    /**
     * Traduce una instrucción de código intermedio a MIPS
     */
    private void traducirInstruccion(String instruccion) {
        instruccion = instruccion.trim();

        if (instruccion.isEmpty()) {
            return;
        }
        
        // Omitir asignaciones de VARIABLES (no arrays) inicializadas globales (antes de cualquier función)
        // Para arrays inicializados, se rastrearán para ejecutarse dentro de main
        if (funcionActual.isEmpty() && instruccion.contains("=") && !instruccion.contains(":")) {
            // Verificar si esta asignación es de una variable global (NO array)
            String[] partes = instruccion.split("(?<![<>!=])=(?![=])");
            if (partes.length == 2) {
                String variable = partes[0].trim();
                
                // Si contiene corchetes, es un array - rastrearlo para inicialización en main
                if (variable.contains("[")) {
                    inicializacionesArrayGlobal.add(instruccion);
                    return;
                }
                
                // Si es una variable global simple, omitir la asignación
                if (variablesGlobales.containsKey(variable)) {
                    return;
                }
            }
        }

        // Detectar comentarios de declaración de arrays: # Declaracion: tipo[d1][d2][d3] nombre
        // Registrar el array en tiempo de traducción (por si no fue capturado en el prepass)
        if (instruccion.startsWith("# Declaracion:") && instruccion.contains("[") && instruccion.contains("]")) {
            // Extraer todas las dimensiones y calcular tamaño total
            // Ejemplo: "# Declaracion: entero[2][3][4] cubo" -> tamaño = 2*3*4 = 24
            int tamañoTotal = 1;
            String tempStr = instruccion;
            
            // Extraer cada dimensión [n] y multiplicar
            while (tempStr.contains("[") && tempStr.contains("]")) {
                int posAb = tempStr.indexOf("[");
                int posCe = tempStr.indexOf("]");
                if (posAb != -1 && posCe != -1 && posCe > posAb) {
                    String dimStr = tempStr.substring(posAb + 1, posCe).trim();
                    try {
                        int dim = Integer.parseInt(dimStr);
                        tamañoTotal *= dim;
                    } catch (NumberFormatException e) {
                        // No es un número, ignorar
                    }
                    tempStr = tempStr.substring(posCe + 1);
                } else {
                    break;
                }
            }
            
            // Extraer nombre del array (última palabra después de los corchetes)
            String[] partes = tempStr.trim().split("\\s+");
            if (partes.length >= 1) {
                String nombreArray = partes[0];
                // Registrar el tamaño si no existe ya
                if (!tamañosArrays.containsKey(nombreArray) && tamañoTotal > 0) {
                    tamañosArrays.put(nombreArray, tamañoTotal);
                    // Pre-reservar espacio en el stack
                    obtenerOffsetVariable(nombreArray, tamañoTotal);
                }
            }
            return;
        }

        // Detectar comentarios de parámetros para registrar sus offsets
        if (instruccion.startsWith("# Parametro:")) {
            String[] partes = instruccion.split("\\s+");
            if (partes.length >= 4) {
                String nombreParam = partes[3];
                // Si ya fue registrado (por el pre-scan) no recalcular
                if (!mapaVariables.containsKey(nombreParam)) {
                    int numParam = mapaVariables.size();
                    int offset = 500 - (numParam * 4); // 500, 496, 492, 488
                    mapaVariables.put(nombreParam, offset);
                }
            }
            return;
        }

        // Ignorar otros comentarios
        if (instruccion.startsWith("#")) {
            return;
        }

        emitir("    # " + instruccion);

        // Etiqueta de función (no comienza con L seguido de dígito)
        if (instruccion.endsWith(":") && !instruccion.matches("L\\d+:")) {
            funcionActual = instruccion.replace(":", "");
            
            // Si es la función principal, generar etiqueta "main" en lugar de "principal"
            if (funcionActual.equals("principal")) {
                emitir("main:");
                funcionActual = "main"; // Actualizar también funcionActual
            } else {
                emitir(instruccion);
            }
            
            // Resetear mapa de variables y parámetros para la nueva función
            mapaVariables.clear();
            parametrosActuales.clear();
            tamañosArrays.clear();
            offsetVariables = 0;

            // Pre-escanear parámetros desde las siguientes líneas del código intermedio
            int contadorParams = 0;
            for (int i = indiceActual + 1; i < codigoIntermedio.size(); i++) {
                String linea = codigoIntermedio.get(i).trim();
                if (linea.startsWith("# Parametro:")) {
                    String[] partes = linea.split("\\s+");
                    if (partes.length >= 4) {
                        String nombreParam = partes[3];
                        int offset = 500 - (contadorParams * 4); // 500, 496, 492, 488
                        parametrosActuales.put(nombreParam, offset);
                        contadorParams++;
                    }
                } else if (!linea.startsWith("#") && !linea.isEmpty()) {
                    break;
                }
            }
            // Cargar los parámetros detectados en el mapa de variables
            mapaVariables.putAll(parametrosActuales);
            
            // Pre-escanear declaraciones de arrays para reservar espacio
            for (int i = indiceActual + 1; i < codigoIntermedio.size(); i++) {
                String linea = codigoIntermedio.get(i).trim();
                if (linea.startsWith("# Declaracion:") && linea.contains("[") && linea.contains("]")) {
                    int posCorcheteAb = linea.indexOf("[");
                    int posCorcheteC = linea.indexOf("]");
                    
                    if (posCorcheteAb != -1 && posCorcheteC != -1) {
                        String tamanosStr = linea.substring(posCorcheteAb + 1, posCorcheteC).trim();
                        String resto = linea.substring(posCorcheteC + 1).trim();
                        String[] partes = resto.split("\\s+");
                        
                        if (partes.length >= 1) {
                            String nombreArray = partes[0];
                            try {
                                int tamaño = Integer.parseInt(tamanosStr);
                                // Registrar el tamaño y asignar offset
                                tamañosArrays.put(nombreArray, tamaño);
                                obtenerOffsetVariable(nombreArray, tamaño);
                            } catch (NumberFormatException e) {
                                // Ignorar
                            }
                        }
                    }
                } else if (linea.endsWith(":") && !linea.matches("L\\d+:")) {
                    // Detener cuando se encuentre otra etiqueta de función (no etiquetas de control de flujo)
                    break;
                }
            }
            
            // Generar prólogo de función
            emitir("    addi $sp, $sp, -1024  # Reservar espacio en stack (1024 bytes para arrays)");
            emitir("    sw $ra, 508($sp)      # Guardar return address");
            emitir("    sw $fp, 504($sp)      # Guardar frame pointer");
            emitir("    move $fp, $sp         # Nuevo frame pointer");
            // Guardar parámetros (los primeros 4 están en $a0-$a3)
            emitir("    sw $a0, 500($fp)      # Guardar primer parámetro");
            emitir("    sw $a1, 496($fp)      # Guardar segundo parámetro");
            emitir("    sw $a2, 492($fp)      # Guardar tercer parámetro");
            emitir("    sw $a3, 488($fp)      # Guardar cuarto parámetro");
        }
        // Etiqueta de control de flujo (L0, L1, etc.)
        else if (instruccion.endsWith(":")) {
            emitir(instruccion);
        }
        // Parámetro de función
        else if (instruccion.startsWith("param")) {
            String[] partes = instruccion.split("\\s+");
            String param = partes[1];
            parametrosPendientes.add(param);
        }
        // Print
        else if (instruccion.startsWith("print")) {
            traducirPrint(instruccion);
        }
        // Read
        else if (instruccion.startsWith("read")) {
            traducirRead(instruccion);
        }
        // Asignación especial con :=
        else if (instruccion.contains(":=")) {
            String[] partes = instruccion.split(":=");
            if (partes.length == 2) {
                String destino = partes[0].trim();
                String expresion = partes[1].trim();
                cargarEnRegistro(expresion, "$t0");
                almacenarVariable(destino, "$t0");
            }
        }
        // Asignación
        else if (instruccion.contains("=")) {
            traducirAsignacion(instruccion);
        }
        // Salto incondicional
        else if (instruccion.startsWith("goto")) {
            String etiqueta = instruccion.split("\\s+")[1];
            emitir("    j " + etiqueta);
        }
        // Salto condicional
        else if (instruccion.startsWith("if")) {
            traducirSaltoCondicional(instruccion);
        }
        // Llamada a función
        else if (instruccion.startsWith("call")) {
            String[] partes = instruccion.split("\\s+");
            String funcion = partes[1].replace(",", "");
            
            // Guardar registros temporales antes de la llamada
            emitir("    addi $sp, $sp, -12");
            emitir("    sw $t0, 0($sp)");
            emitir("    sw $t1, 4($sp)");
            emitir("    sw $t2, 8($sp)");
            
            // Pasar parámetros
            for (int i = 0; i < parametrosPendientes.size() && i < 4; i++) {
                String param = parametrosPendientes.get(i);
                cargarEnRegistro(param, "$a" + i);
            }
            parametrosPendientes.clear();
            
            emitir("    jal " + funcion);
            
            // Restaurar registros temporales
            emitir("    lw $t0, 0($sp)");
            emitir("    lw $t1, 4($sp)");
            emitir("    lw $t2, 8($sp)");
            emitir("    addi $sp, $sp, 12");
            // El resultado está en $v0
        }
        // Retorno
        else if (instruccion.startsWith("return")) {
            traducirRetorno(instruccion);
        }
        
        emitir();
    }
    
    /**
     * Traduce una asignación
     */
    private void traducirAsignacion(String instruccion) {
        // Buscar el = que no sea parte de <=, >=, ==, !=, :=
        int posIgual = -1;
        for (int i = 0; i < instruccion.length(); i++) {
            if (instruccion.charAt(i) == '=') {
                boolean esOperador = false;
                if (i > 0 && (instruccion.charAt(i-1) == '<' || instruccion.charAt(i-1) == '>' || 
                              instruccion.charAt(i-1) == '=' || instruccion.charAt(i-1) == '!' ||
                              instruccion.charAt(i-1) == ':')) {
                    esOperador = true;
                }
                if (i < instruccion.length() - 1 && instruccion.charAt(i+1) == '=') {
                    esOperador = true;
                }
                if (!esOperador) {
                    posIgual = i;
                    break;
                }
            }
        }
        
        if (posIgual == -1) return;
        
        String destino = instruccion.substring(0, posIgual).trim();
        String expresion = instruccion.substring(posIgual + 1).trim();
        
        // Operación relacional (comparación)
        if (expresion.contains("<") || expresion.contains(">") || 
            expresion.contains("==") || expresion.contains("!=") ||
            expresion.contains("<=") || expresion.contains(">=")) {
            traducirComparacion(destino, expresion);
        }
        // Operación lógica AND
        else if (expresion.contains("&&")) {
            traducirOperacionLogica(destino, expresion, "&&");
        }
        // Operación lógica OR
        else if (expresion.contains("||")) {
            traducirOperacionLogica(destino, expresion, "||");
        }
        // Operación unaria NOT
        else if (expresion.startsWith("!")) {
            traducirOperacionNOT(destino, expresion);
        }
        // Operación aritmética
        else if (expresion.contains("+") || expresion.contains("-") || 
            expresion.contains("*") || expresion.contains("/")) {
            
            String[] ops = expresion.split("\\s+");
            if (ops.length == 3) {
                String op1 = ops[0];
                String operador = ops[1];
                String op2 = ops[2];
                
                cargarEnRegistro(op1, "$t0");
                cargarEnRegistro(op2, "$t1");
                
                switch (operador) {
                    case "+":
                        emitir("    add $t2, $t0, $t1");
                        break;
                    case "-":
                        emitir("    sub $t2, $t0, $t1");
                        break;
                    case "*":
                        emitir("    mul $t2, $t0, $t1");
                        break;
                    case "/":
                        emitir("    div $t0, $t1");
                        emitir("    mflo $t2");
                        break;
                }
                
                almacenarVariable(destino, "$t2");
            }
        }
        // Asignación de call_result
        else if (expresion.equals("call_result")) {
            // El resultado está en $v0
            almacenarVariable(destino, "$v0");
        }
        // Asignación de cadena
        else if (expresion.startsWith("str_")) {
            // Guardar la asociación y almacenar el puntero a la cadena en el stack
            variablesCadena.put(destino, expresion);
            emitir("    la $t0, " + expresion);
            almacenarVariable(destino, "$t0");
        }
        // Asignación simple
        else {
            cargarEnRegistro(expresion, "$t0");
            almacenarVariable(destino, "$t0");
        }
    }
    
    /**
     * Traduce una instrucción print
     */
    private void traducirPrint(String instruccion) {
        // Determinar el tipo de print: print_entero, print_cadena, print_caracter, print_booleano
        String tipo = "entero"; // Por defecto
        String comando = "print";
        
        if (instruccion.contains("_")) {
            String[] partesComando = instruccion.split("\\s+", 2);
            if (partesComando[0].startsWith("print_")) {
                tipo = partesComando[0].substring(6); // Extraer tipo después de "print_"
                instruccion = "print " + (partesComando.length > 1 ? partesComando[1] : "");
            }
        }
        
        String[] partes = instruccion.split("\\s+", 2);
        if (partes.length > 1) {
            String valor = partes[1];
            
            // Si es una etiqueta de cadena directa (str_0, str_1, etc.)
            if (valor.startsWith("str_") || tipo.equals("cadena")) {
                if (valor.startsWith("str_")) {
                    emitir("    la $a0, " + valor);
                } else {
                    cargarEnRegistro(valor, "$a0");
                }
                emitir("    li $v0, 4");
                emitir("    syscall");
            }
            // Si es un carácter (imprimir como entero - código ASCII)
            else if (tipo.equals("caracter")) {
                cargarEnRegistro(valor, "$a0");
                emitir("    li $v0, 11"); // syscall 11: print character
                emitir("    syscall");
            }
            // Si es un booleano o entero
            else {
                cargarEnRegistro(valor, "$a0");
                emitir("    li $v0, 1");
                emitir("    syscall");
            }
            // Imprimir newline
            emitir("    la $a0, newline");
            emitir("    li $v0, 4");
            emitir("    syscall");
        }
    }
    
    /**
     * Traduce una instrucción read
     */
    private void traducirRead(String instruccion) {
        // Determinar el tipo de read: read_entero, read_cadena, read_caracter, read_booleano
        String tipo = "entero"; // Por defecto
        
        if (instruccion.contains("_")) {
            String[] partesComando = instruccion.split("\\s+", 2);
            if (partesComando[0].startsWith("read_")) {
                tipo = partesComando[0].substring(5); // Extraer tipo después de "read_"
                instruccion = "read " + (partesComando.length > 1 ? partesComando[1] : "");
            }
        }
        
        String[] partes = instruccion.split("\\s+", 2);
        if (partes.length > 1) {
            String variable = partes[1];
            
            emitir("");
            emitir("    # " + instruccion);
            
            if (tipo.equals("cadena")) {
                // Leer cadena (syscall 8)
                emitir("    li $v0, 8");
                emitir("    la $a0, buffer");  // Buffer para almacenar la cadena
                emitir("    li $a1, 256");     // Máximo 256 caracteres
                emitir("    syscall");
                // Guardar la dirección del buffer en la variable
                emitir("    la $t0, buffer");
                almacenarVariable(variable, "$t0");
            } else if (tipo.equals("caracter")) {
                // Leer carácter (syscall 12)
                emitir("    li $v0, 12");
                emitir("    syscall");
                emitir("    move $t0, $v0");
                almacenarVariable(variable, "$t0");
            } else {
                // Leer entero o booleano (syscall 5)
                emitir("    li $v0, 5");
                emitir("    syscall");
                emitir("    move $t0, $v0");
                almacenarVariable(variable, "$t0");
            }
        }
    }
    
    /**
     * Traduce un salto condicional
     */
    private void traducirSaltoCondicional(String instruccion) {
        String[] partes = instruccion.split("\\s+");
        
        if (instruccion.startsWith("ifFalse")) {
            // ifFalse x goto L1 -> salta si x es cero (falso)
            if (partes.length >= 4) {
                String condicion = partes[1];
                String etiqueta = partes[3];
                
                cargarEnRegistro(condicion, "$t0");
                emitir("    beqz $t0, " + etiqueta); // Salta si es cero
            }
        } else {
            // if x goto L1 -> salta si x es distinto de cero (verdadero)
            if (partes.length >= 3) {
                String condicion = partes[1];
                String etiqueta = partes[3];
                
                cargarEnRegistro(condicion, "$t0");
                emitir("    bnez $t0, " + etiqueta); // Salta si NO es cero
            }
        }
    }
    
    /**
     * Traduce una comparación relacional
     */
    private void traducirComparacion(String destino, String expresion) {
        String[] ops = expresion.split("\\s+");
        if (ops.length == 3) {
            String op1 = ops[0];
            String operador = ops[1];
            String op2 = ops[2];
            
            cargarEnRegistro(op1, "$t0");
            cargarEnRegistro(op2, "$t1");
            
            switch (operador) {
                case "<":
                    emitir("    slt $t2, $t0, $t1"); // set less than
                    break;
                case ">":
                    // a > b  es lo mismo que  b < a
                    emitir("    slt $t2, $t1, $t0"); // set greater than
                    break;
                case "<=":
                    // a <= b  es lo mismo que  !(a > b)  que es  !(b < a)
                    emitir("    slt $t2, $t1, $t0");
                    emitir("    xori $t2, $t2, 1"); // NOT lógico
                    break;
                case ">=":
                    // a >= b  es lo mismo que  !(a < b)
                    emitir("    slt $t2, $t0, $t1");
                    emitir("    xori $t2, $t2, 1"); // NOT lógico
                    break;
                case "==":
                    // a == b: XOR da 0 si son iguales, luego verificar si es menor que 1
                    emitir("    xor $t2, $t0, $t1");
                    emitir("    sltiu $t2, $t2, 1"); // 1 si t2 == 0
                    break;
                case "!=":
                    // a != b: XOR da 0 si son iguales, verificar si NO es 0
                    emitir("    xor $t2, $t0, $t1");
                    emitir("    sltu $t2, $zero, $t2"); // 1 si t2 != 0
                    break;
            }
            
            almacenarVariable(destino, "$t2");
        }
    }
    
    /**
     * Carga un valor o variable en un registro
     */
    private void cargarEnRegistro(String fuente, String registro) {
        // Detectar acceso a array: nombre[índice]
        if (fuente.contains("[") && fuente.contains("]")) {
            int posCorchete = fuente.indexOf("[");
            String nombreArray = fuente.substring(0, posCorchete).trim();
            int posCorcheteC = fuente.indexOf("]");
            String indiceStr = fuente.substring(posCorchete + 1, posCorcheteC).trim();
            
            // Obtener el offset del array base
            int offsetArray = obtenerOffsetVariable(nombreArray);
            
            // Cargar el valor del índice
            if (esNumero(indiceStr)) {
                int indice = Integer.parseInt(indiceStr);
                int offsetElemento = offsetArray - (indice * 4); // Arrays decrecen en el stack
                emitir("    lw " + registro + ", " + offsetElemento + "($fp)");
            } else {
                // El índice es una variable
                cargarEnRegistro(indiceStr, "$t8");
                // Calcular offset: offsetArray - (índice * 4)
                emitir("    li $t9, 4");
                emitir("    mul $t8, $t8, $t9");
                emitir("    li $t9, " + offsetArray);
                emitir("    sub $t8, $t9, $t8");
                emitir("    add $t8, $t8, $fp");
                emitir("    lw " + registro + ", 0($t8)");
            }
        } else if (esNumero(fuente)) {
            emitir("    li " + registro + ", " + fuente);
        } else if (fuente.startsWith("str_")) {
            // Es una etiqueta de cadena literal
            emitir("    la " + registro + ", " + fuente);
        } else if (variablesCadena.containsKey(fuente)) {
            // Cargar la dirección de la cadena asociada
            String etiqueta = variablesCadena.get(fuente);
            emitir("    la " + registro + ", " + etiqueta);
        } else if (variablesGlobales.containsKey(fuente)) {
            // Cargar desde variable global
            emitir("    la $t9, " + fuente);
            emitir("    lw " + registro + ", 0($t9)");
        } else {
            int offset = obtenerOffsetVariable(fuente);
            emitir("    lw " + registro + ", " + offset + "($fp)");
        }
    }
    
    /**
     * Almacena un registro en una variable
     */
    private void almacenarVariable(String variable, String registro) {
        // Detectar si es asignación a un array: nombre[índice]
        if (variable.contains("[") && variable.contains("]")) {
            int posCorchete = variable.indexOf("[");
            String nombreArray = variable.substring(0, posCorchete).trim();
            int posCorcheteC = variable.indexOf("]");
            String indiceStr = variable.substring(posCorchete + 1, posCorcheteC).trim();
            
            // Obtener el offset del array base
            int offsetArray = obtenerOffsetVariable(nombreArray);
            
            if (esNumero(indiceStr)) {
                int indice = Integer.parseInt(indiceStr);
                int offsetElemento = offsetArray - (indice * 4); // Arrays decrecen en el stack
                emitir("    sw " + registro + ", " + offsetElemento + "($fp)");
            } else {
                // El índice es una variable
                cargarEnRegistro(indiceStr, "$t8");
                // Calcular offset: offsetArray - (índice * 4)
                emitir("    li $t9, 4");
                emitir("    mul $t8, $t8, $t9");
                emitir("    li $t9, " + offsetArray);
                emitir("    sub $t8, $t9, $t8");
                emitir("    add $t8, $t8, $fp");
                emitir("    sw " + registro + ", 0($t8)");
            }
        } else if (variablesGlobales.containsKey(variable)) {
            // Almacenar en variable global
            emitir("    la $t9, " + variable);
            emitir("    sw " + registro + ", 0($t9)");
        } else {
            int offset = obtenerOffsetVariable(variable);
            emitir("    sw " + registro + ", " + offset + "($fp)");
        }
    }
    
    /**
     * Obtiene el offset de una variable en el stack frame
     */
    private int obtenerOffsetVariable(String variable) {
        // Si es un array conocido, usar su tamaño
        if (tamañosArrays.containsKey(variable)) {
            return obtenerOffsetVariable(variable, tamañosArrays.get(variable));
        }
        return obtenerOffsetVariable(variable, 1);
    }
    
    /**
     * Obtiene el offset de una variable en el stack frame
     * @param variable nombre de la variable o array
     * @param tamaño tamaño en elementos (1 para variables normales, >1 para arrays)
     */
    private int obtenerOffsetVariable(String variable, int tamaño) {
        // Usar offset ya registrado si existe
        if (mapaVariables.containsKey(variable)) {
            return mapaVariables.get(variable);
        }

        // Si es un parámetro detectado en el pre-scan, reutilizar su offset
        if (parametrosActuales.containsKey(variable)) {
            int offsetParam = parametrosActuales.get(variable);
            mapaVariables.put(variable, offsetParam);
            return offsetParam;
        }

        // Variables locales: asignar nuevo espacio decreciendo desde 512($fp)
        // Nota: En MIPS, todos los tipos (entero, cadena, carácter, booleano) se alinean a 4 bytes (1 palabra)
        // Por lo que decrementamos siempre de 4 en 4 bytes
        // Para arrays, el offset devuelto es el del PRIMER elemento [0]
        // pero reservamos espacio para TODOS los elementos
        offsetVariables -= 4;
        int nuevoOffset = 1024 + offsetVariables;  // Offset base 1024 para 1024-byte stack
        mapaVariables.put(variable, nuevoOffset);
        
        // Si es un array, reservar espacio adicional para los elementos restantes
        if (tamaño > 1) {
            offsetVariables -= (tamaño - 1) * 4;
        }
        
        return nuevoOffset;
    }
    
    /**
     * Traduce una operación lógica (AND/OR)
     */
    private void traducirOperacionLogica(String destino, String expresion, String operador) {
        String[] partes = expresion.split(operador.equals("&&") ? "&&" : "\\|\\|");
        if (partes.length == 2) {
            String op1 = partes[0].trim();
            String op2 = partes[1].trim();
            
            cargarEnRegistro(op1, "$t0");
            cargarEnRegistro(op2, "$t1");
            
            if (operador.equals("&&")) {
                // AND: resultado es 1 si ambos son != 0
                emitir("    # AND operation");
                emitir("    sltu $t2, $zero, $t0");  // $t2 = 1 si $t0 != 0
                emitir("    sltu $t3, $zero, $t1");  // $t3 = 1 si $t1 != 0
                emitir("    and $t2, $t2, $t3"); // $t2 = $t2 AND $t3
            } else {
                // OR: resultado es 1 si al menos uno es != 0
                emitir("    # OR operation");
                emitir("    sltu $t2, $zero, $t0");  // $t2 = 1 si $t0 != 0
                emitir("    sltu $t3, $zero, $t1");  // $t3 = 1 si $t1 != 0
                emitir("    or $t2, $t2, $t3"); // $t2 = $t2 OR $t3
            }
            
            almacenarVariable(destino, "$t2");
        }
    }
    
    /**
     * Traduce una operación NOT (negación)
     */
    private void traducirOperacionNOT(String destino, String expresion) {
        String operando = expresion.substring(1).trim(); // Quitar el !
        
        cargarEnRegistro(operando, "$t0");
        
        emitir("    # NOT operation");
        emitir("    sltiu $t2, $t0, 1");  // $t2 = 1 si $t0 < 1 (== 0), 0 si $t0 >= 1
        
        almacenarVariable(destino, "$t2");
    }
    
    /**
     * Traduce una instrucción return
     */
    private void traducirRetorno(String instruccion) {
        String[] partes = instruccion.split("\\s+");
        if (partes.length > 1) {
            String valor = partes[1];
            if (!valor.equals("0")) { // Solo cargar si no es 0 (valor por defecto)
                cargarEnRegistro(valor, "$v0"); // Poner valor de retorno en $v0
            }
        }
        
        if (funcionActual.equals("main")) {
            // Epílogo de main y salir del programa
            emitir("    move $sp, $fp         # Restaurar stack pointer");
            emitir("    lw $ra, 508($sp)      # Restaurar return address");
            emitir("    lw $fp, 504($sp)      # Restaurar frame pointer");
            emitir("    addi $sp, $sp, 1024   # Liberar espacio en stack");
            emitir("    li $v0, 10            # syscall exit");
            emitir("    syscall");
        } else {
            // Epílogo de función y retorno
            emitir("    move $sp, $fp         # Restaurar stack pointer");
            emitir("    lw $ra, 508($sp)      # Restaurar return address");
            emitir("    lw $fp, 504($sp)      # Restaurar frame pointer");
            emitir("    addi $sp, $sp, 1024   # Liberar espacio en stack");
            emitir("    jr $ra                # Retornar");
        }
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
     * Emite una línea de código ensamblador
     */
    private void emitir(String linea) {
        codigoEnsamblador.add(linea);
    }
    
    /**
     * Emite una línea vacía
     */
    private void emitir() {
        codigoEnsamblador.add("");
    }
    
    /**
     * Volca el código ensamblador a un archivo
     */
    public void volcarAArchivo(String rutaArchivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            for (String linea : codigoEnsamblador) {
                writer.println(linea);
            }
        }
    }
    
    /**
     * Imprime el código ensamblador en consola
     */
    public void imprimir() {
        System.out.println("\n=== CÓDIGO ENSAMBLADOR MIPS ===");
        for (String linea : codigoEnsamblador) {
            System.out.println(linea);
        }
    }
}
