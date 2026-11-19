package com.simulator;

public class Modelos {

    public enum TipoToken {
        PALABRA_CLAVE,
        TIPO_DATO,
        IDENTIFICADOR,
        NUMERO,
        CADENA,
        OPERADOR_ASIGNACION,
        OPERADOR_ARITMETICO,
        OPERADOR_RELACIONAL,
        PUNTO_Y_COMA,
        COMA,
        PARENTESIS_ABRE,
        PARENTESIS_CIERRA,
        DESCONOCIDO,
        EOF
    }

    public enum TipoError {
        LEXICO("Léxico"),
        SINTACTICO("Sintáctico"),
        SEMANTICO("Semántico");

        public final String etiqueta;

        TipoError(String etiqueta) {
            this.etiqueta = etiqueta;
        }
    }

    public static class Token {
        public final TipoToken tipo;
        public final String lexema;
        public final int linea;

        public Token(TipoToken tipo, String lexema, int linea) {
            this.tipo = tipo;
            this.lexema = lexema;
            this.linea = linea;
        }

        @Override
        public String toString() {
            return String.format("[Línea %d] %s -> '%s'", linea, tipo, lexema);
        }
    }

    public static class ErrorCompilador {
        public final String codigo;
        public final TipoError tipoError;
        public final int linea;
        public final String mensaje;

        public ErrorCompilador(String codigo, TipoError tipoError, int linea, String mensaje) {
            this.codigo = codigo;
            this.tipoError = tipoError;
            this.linea = linea;
            this.mensaje = mensaje;
        }

        @Override
        public String toString() {
            return String.format("Error [%s] %s – Línea %d: %s", codigo, tipoError.etiqueta, linea, mensaje);
        }
    }
}