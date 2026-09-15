package com.compilador.ast;

import com.compilador.backend.GeneradorCodigo;
import com.compilador.semantico.TipoDato;
import java.util.List;

/**
 * Nodo para declaraciones de funciones
 */
public class NodoFuncion extends Nodo {
    private TipoDato tipoRetorno;
    private String nombre;
    private List<NodoDeclaracionVariable> parametros;
    private NodoBloque cuerpo;
    
    public NodoFuncion(TipoDato tipo, String nombre, List<NodoDeclaracionVariable> params, 
                       NodoBloque cuerpo, int linea) {
        super(linea);
        this.tipoRetorno = tipo;
        this.nombre = nombre;
        this.parametros = params;
        this.cuerpo = cuerpo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public TipoDato getTipoRetorno() {
        return tipoRetorno;
    }
    
    public List<NodoDeclaracionVariable> getParametros() {
        return parametros;
    }
    
    public NodoBloque getCuerpo() {
        return cuerpo;
    }
    
    @Override
    public String generarCodigo(GeneradorCodigo gen) {
        gen.emitir("");
        gen.emitir("# Funcion: " + tipoRetorno + " " + nombre);
        gen.emitirEtiqueta(nombre);
        
        // Código para parámetros
        for (NodoDeclaracionVariable param : parametros) {
            gen.emitir("# Parametro: " + param.getTipoVariable() + " " + param.getNombre());
        }
        
        // Cuerpo de la función
        if (cuerpo != null) {
            cuerpo.generarCodigo(gen);
        }
        
        // Si es una función vacio sin devolver explícito, agregamos el retorno
        if (tipoRetorno == TipoDato.VOID) {
            gen.emitir("return");
        }
        
        return null;
    }
}
