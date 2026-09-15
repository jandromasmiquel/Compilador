#  Compilador Completo: Lenguaje Imperativo ➔ Ensamblador MIPS

Un compilador completo desarrollado en Java utilizando **JFlex** (análisis léxico) y **Java CUP** (análisis sintáctico LALR). Traduce un lenguaje imperativo de alto nivel con soporte para arreglos multidimensionales, estructuras de control, funciones recursivas y paso de parámetros a **Código Intermedio de 3 Direcciones (TAC)** y finalmente a **Ensamblador MIPS**.


##  Arquitectura del Compilador

El compilador sigue una arquitectura clásica de múltiples fases:

```
                                  [ Código Fuente (.txt) ]
                                             │
                                             ▼
                                  [ 1. Análisis Léxico ] (JFlex)
                                             │
                                             ▼  Tokens (output/tokens.txt)
                                  [ 2. Análisis Sintáctico ] (Java CUP)
                                             │
                                             ▼  Árbol de Sintaxis Abst. (AST)
                                  [ 3. Análisis Semántico ]
                                             │  - Comprobador de tipos
                                             │  - Tabla de símbolos y ámbitos
                                             ▼
                                  [ 4. Código Intermedio ]
                                             │  - Generación de Código 3 Direcciones (TAC)
                                             ▼
                                  [ 5. Optimización ]
                                             │  - Propagación y plegado de constantes
                                             │  - Eliminación de código muerto
                                             ▼
                                  [ 6. Generación MIPS ]
                                             │
                                             ▼
                          [ Ensamblador MIPS (.asm) / MARS / SPIM ]
```

---

##  Características Destacadas del Lenguaje

- **Tipos de Datos**: `entero`, `cadena`, `booleano`, `caracter`, `vacio`.
- **Modificador de Constantes**: Soporte para `constante` globales y de ámbito local.
- **Estructuras de Control**:
  - Condicionales: `si (condicion) { ... } sino { ... }`
  - Bucles: `mientras (condicion) { ... }` y `para (inicializacion; condicion; incremento) { ... }`
- **Arreglos Multidimensionales**:
  - Arreglos de 1D, 2D y 3D (`entero[2][3][4] cubo;`).
  - Inicialización literal jerárquica: `entero[2][2][2] cubo_init = {[[1, 2], [3, 4]], [[5, 6], [7, 8]]};`.
  - Acceso y modificación por índice (`cubo[1][2][3] = 42;`).
- **Funciones y Procedimientos**:
  - Procedimientos `vacio` y funciones con retorno de valor.
  - Soporte para **recursividad** (p. ej., cálculo factorial).
  - Manejo de ámbitos (variables globales vs. locales) y pila de llamadas.
- **Entrada / Salida**:
  - `escribir(...)`: Imprime enteros, cadenas, caracteres y booleanos.
  - `leer(...)`: Lectura desde consola.

---

##  Estructura del Proyecto

```
PracticaCompiladores/
├── build.bat                 # Script de compilación y ejecución automática para Windows
├── build.sh                  # Script de compilación y ejecución automática para Linux/macOS
├── .gitignore                # Exclusión de binarios compilados y temporales
├── README.md                 # Documentación del proyecto
├── lib/                      # Librerías necesarias (JFlex y Java CUP)
│   ├── java-cup-11b.jar
│   ├── java-cup-11b-runtime.jar
│   └── jflex-full-1.9.1.jar
├── src/                      # Código fuente en Java
│   └── com/compilador/
│       ├── Main.java         # Orquestador de las fases del compilador
│       ├── ast/              # Nodos del Árbol de Sintaxis Abst. (25+ tipos de nodos)
│       ├── backend/          # Generador de TAC, Optimizador y Generador MIPS
│       ├── herramientas/     # Gestor de errores y representación de Tokens
│       ├── lexico/           # Especificación JFlex (Scanner.flex y Scanner.java)
│       ├── semantico/        # Tabla de Símbolos y Comprobador de Tipos
│       └── sintactico/       # Gramática CUP (Parser.cup, Parser.java y sym.java)
├── input/                    # Casos de prueba (.txt)
│   ├── prueba_definitiva.txt # Test exhaustivo (arrays 3D, recursividad, ordenación)
│   └── error_*.txt           # Pruebas de comprobación de errores semánticos
├── output/                   # Artefactos generados durante la compilación
│   ├── tokens.txt            # Secuencia completa de tokens analizados
│   ├── tabla_simbolos.txt    # Volcado de la tabla de símbolos
│   ├── codigo_intermedio.txt # Código TAC de 3 direcciones
│   ├── codigo_optimizado.asm # Código Ensamblador MIPS resultante
│   └── errores.txt           # Reporte de errores sintácticos/semánticos
└── documentos/               # Documentación académica y presentación PDF
    ├── Documentación.pdf
    └── PRESENTACION.pdf
```

---

##  Compilación y Ejecución

### Requisitos Previos
- **Java JDK 8** o superior instalado en el sistema.

### Ejecución Rápida

#### En Windows (PowerShell / CMD):
```cmd
.\build.bat
```
*(Para probar un archivo específico: `.\build.bat input/error_arrays.txt`)*

#### En Linux / macOS (Bash):
```bash
chmod +x build.sh
./build.sh
```
*(Para probar un archivo específico: `./build.sh input/error_arrays.txt`)*

---

##  Regenerar el Léxico (JFlex) o Sintáctico (CUP)

Si modificas la especificación léxica (`Scanner.flex`) o la gramática (`Parser.cup`):

### Regenerar Scanner léxico:
```bash
java -jar lib/jflex-full-1.9.1.jar src/com/compilador/lexico/Scanner.flex
```

### Regenerar Parser sintáctico:
```bash
java -jar lib/java-cup-11b.jar -destdir src/com/compilador/sintactico src/com/compilador/sintactico/Parser.cup
```

---

##  Ejemplo de Código Fuente y Salida MIPS

### Entrada (`input/prueba_definitiva.txt` fragmento):
```c
entero factorial(entero n) {
    si (n <= 1) {
        devolver 1;
    } sino {
        devolver n * factorial(n - 1);
    }
}
```

### Salida de Código Intermedio TAC (`output/codigo_intermedio.txt`):
```text
FUNCION factorial:
  t0 = n <= 1
  SI_FALSO t0 GOTO L0
  DEVOLVER 1
  GOTO L1
L0:
  t1 = n - 1
  PARAMETRO t1
  t2 = LLAMADA factorial, 1
  t3 = n * t2
  DEVOLVER t3
L1:
FIN_FUNCION
```