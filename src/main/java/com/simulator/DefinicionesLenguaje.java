package com.simulator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.simulator.Modelos.TipoError;

public class DefinicionesLenguaje {

    public static final String REGEX_IDENTIFICADOR = "^[A-Za-z][A-Za-z0-9_]*$";

    public static final Set<String> PALABRAS_CLAVE = new HashSet<>(Arrays.asList(
            "inicio", "fin", "declare", "lea", "escriba",
            "si", "entonces", "sino", "fsi",
            "para", "hasta", "haga", "fpara",
            "mq", "fmq"
    ));

    public static final Set<String> TIPOS_DATO = new HashSet<>(Arrays.asList(
            "entero", "real", "cadena", "fecha", "lógico", "logico"
    ));

    public static boolean esPalabraReservada(String lexema) {
        String norm = lexema.toLowerCase(Locale.ROOT);
        return PALABRAS_CLAVE.contains(norm) || TIPOS_DATO.contains(norm);
    }

    public static boolean esTipoDatoValido(String lexema) {
        return TIPOS_DATO.contains(lexema.toLowerCase(Locale.ROOT));
    }

    public static boolean esIdentificadorValido(String lexema) {
        return lexema != null && lexema.matches(REGEX_IDENTIFICADOR) && !esPalabraReservada(lexema);
    }

    public static String normalizarTipo(String tipo) {
        String norm = tipo.toLowerCase(Locale.ROOT);
        if (norm.equals("logico")) {
            return "Lógico";
        }
        return tipo.substring(0, 1).toUpperCase(Locale.ROOT) + tipo.substring(1).toLowerCase(Locale.ROOT);
    }

    public static TipoError tipoDeError(String codigo) {
        switch (codigo) {
            case "E01":
            case "E02":
                return TipoError.LEXICO;
            case "E14":
            case "E15":
            case "E16":
                return TipoError.SEMANTICO;
            default:
                return TipoError.SINTACTICO;
        }
    }

    public static String obtenerDescripcionError(String codigo) {
        switch (codigo) {
            case "E01": return "Carácter no permitido en el lenguaje.";
            case "E02": return "Identificador inválido (no cumple la expresión regular ^[A-Za-z][A-Za-z0-9_]*$ o usa palabra reservada).";
            case "E03": return "Falta la palabra 'Inicio' o aparece contenido antes de ella.";
            case "E04": return "Falta la palabra 'Fin' o aparece contenido después de ella.";
            case "E05": return "Falta ';' al final de la sentencia.";
            case "E06": return "Declaración inválida: revise el tipo de dato o las comas en la lista.";
            case "E07": return "Instrucción de asignación, Lea o Escriba mal formada.";
            case "E08": return "Condición inválida: se esperan dos operandos y un operador relacional válido (>, <, <>, >=, <=, =).";
            case "E09": return "Falta un componente obligatorio del bloque (Entonces, Haga o Hasta).";
            case "E10": return "Bloque sin cierre correspondiente (falta FSi, FMQ o FPara).";
            case "E11": return "Cierre sin apertura o estructura 'Sino' inválida/duplicada.";
            case "E12": return "Paréntesis desbalanceados en la expresión.";
            case "E13": return "Expresión mal formada (operador sin operando, operandos seguidos o paréntesis vacíos).";
            case "E14": return "Variable no declarada.";
            case "E15": return "Variable ya declarada anteriormente.";
            case "E16": return "La variable o un índice del ciclo 'Para' debe ser de tipo Entero.";
            default:    return "Error de sintaxis no especificado.";
        }
    }
}