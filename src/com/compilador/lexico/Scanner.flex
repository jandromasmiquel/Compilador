package com.compilador.lexico;

import java_cup.runtime.*;
import com.compilador.sintactico.sym;

/**
 * Especificación JFlex para el análisis léxico
 * Este archivo debe ser procesado con JFlex para generar Scanner.java
 * 
 * Comando: java -jar lib/jflex-full-1.9.1.jar src/com/compilador/lexico/Scanner.flex
 */

%%

%public
%class Scanner
%unicode
%cup
%line
%column

%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

/* Expresiones regulares */
ESPACIO = [ \t\r\n]+
COMENTARIO = "//".*
ENTERO = [0-9]+
IDENTIFICADOR = [a-zA-Z_][a-zA-Z0-9_]*
CADENA = \"([^\\\"]|\\.)*\"
CARACTER = '([^'\\\n]|\\.)'

%%

/* Palabras reservadas */
"entero"        { return symbol(sym.ENTERO_TIPO); }
"cadena"        { return symbol(sym.CADENA_TIPO); }
"caracter"      { return symbol(sym.CARACTER_TIPO); }
"booleano"      { return symbol(sym.BOOLEANO_TIPO); }
"vacio"         { return symbol(sym.VACIO_TIPO); }
"constante"     { return symbol(sym.CONSTANTE); }
"verdadero"     { return symbol(sym.VERDADERO); }
"falso"         { return symbol(sym.FALSO); }
"si"            { return symbol(sym.SI); }
"sino"          { return symbol(sym.SINO); }
"mientras"      { return symbol(sym.MIENTRAS); }
"para"          { return symbol(sym.PARA); }
"devolver"      { return symbol(sym.DEVOLVER); }
"escribir"      { return symbol(sym.ESCRIBIR); }
"leer"          { return symbol(sym.LEER); }

/* Operadores */
"+"             { return symbol(sym.MAS); }
"-"             { return symbol(sym.MENOS); }
"*"             { return symbol(sym.POR); }
"/"             { return symbol(sym.DIV); }
"="             { return symbol(sym.ASIGNAR); }
"=="            { return symbol(sym.IGUAL); }
"!="            { return symbol(sym.DIFERENTE); }
"<"             { return symbol(sym.MENOR); }
">"             { return symbol(sym.MAYOR); }
"<="            { return symbol(sym.MENORIGUAL); }
">="            { return symbol(sym.MAYORIGUAL); }
"&&"            { return symbol(sym.AND); }
"||"            { return symbol(sym.OR); }
"!"             { return symbol(sym.NOT); }

/* Delimitadores */
"("             { return symbol(sym.PAREN_IZQ); }
")"             { return symbol(sym.PAREN_DER); }
"{"             { return symbol(sym.LLAVE_IZQ); }
"}"             { return symbol(sym.LLAVE_DER); }
"["             { return symbol(sym.CORCHETE_IZQ); }
"]"             { return symbol(sym.CORCHETE_DER); }
"."             { return symbol(sym.PUNTO); }
";"             { return symbol(sym.PUNTO_COMA); }
","             { return symbol(sym.COMA); }

/* Literales e identificadores */
{ENTERO}        { return symbol(sym.NUMERO, Integer.parseInt(yytext())); }
{CADENA}        { return symbol(sym.CADENA, yytext()); }
{CARACTER}      { return symbol(sym.CARACTER_LIT, yytext()); }
{IDENTIFICADOR} { return symbol(sym.ID, yytext()); }

/* Ignorar */
{ESPACIO}       { /* Ignorar espacios en blanco */ }
{COMENTARIO}    { /* Ignorar comentarios */ }

/* Error */
.               { throw new Error("Carácter ilegal: " + yytext() + " en línea " + yyline); }
