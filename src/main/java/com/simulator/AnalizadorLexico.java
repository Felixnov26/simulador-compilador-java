package com.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.simulator.Modelos.ErrorCompilador;
import com.simulator.Modelos.TipoError;
import com.simulator.Modelos.TipoToken;
import com.simulator.Modelos.Token;

public class AnalizadorLexico {

    private final String codigo;
    public final List<Token> tokens = new ArrayList<>();
    public final List<ErrorCompilador> errores = new ArrayList<>();

    public AnalizadorLexico(String codigo) {
        this.codigo = codigo;
    }

    private void agregarError(String codigoErr, int linea, String extra) {
        TipoError tipo = DefinicionesLenguaje.tipoDeError(codigoErr);
        String msg = DefinicionesLenguaje.obtenerDescripcionError(codigoErr);
        if (extra != null && !extra.isEmpty()) {
            msg = msg + " " + extra;
        }
        errores.add(new ErrorCompilador(codigoErr, tipo, linea, msg));
    }

    public void tokenizar() {
        int i = 0;
        int n = codigo.length();
        int linea = 1;

        while (i < n) {
            char c = codigo.charAt(i);

            if (c == '\n') {
                linea++;
                i++;
                continue;
            }
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '/' && i + 1 < n && codigo.charAt(i + 1) == '/') {
                while (i < n && codigo.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
                int inicio = i;
                while (i < n) {
                    char ch = codigo.charAt(i);
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_') {
                        i++;
                    } else {
                        break;
                    }
                }
                String lexema = codigo.substring(inicio, i);
                String norm = lexema.toLowerCase(Locale.ROOT);

                if (DefinicionesLenguaje.PALABRAS_CLAVE.contains(norm)) {
                    tokens.add(new Token(TipoToken.PALABRA_CLAVE, lexema, linea));
                } else if (DefinicionesLenguaje.TIPOS_DATO.contains(norm)) {
                    tokens.add(new Token(TipoToken.TIPO_DATO, lexema, linea));
                } else if (lexema.matches(DefinicionesLenguaje.REGEX_IDENTIFICADOR)) {
                    tokens.add(new Token(TipoToken.IDENTIFICADOR, lexema, linea));
                } else {
                    agregarError("E02", linea, "Lexema: '" + lexema + "'.");
                    tokens.add(new Token(TipoToken.DESCONOCIDO, lexema, linea));
                }
                continue;
            }

            if (c >= '0' && c <= '9') {
                int inicio = i;
                boolean tienePunto = false;
                while (i < n) {
                    char ch = codigo.charAt(i);
                    if (ch >= '0' && ch <= '9') {
                        i++;
                    } else if (ch == '.' && !tienePunto && i + 1 < n && Character.isDigit(codigo.charAt(i + 1))) {
                        tienePunto = true;
                        i++;
                    } else {
                        break;
                    }
                }
                if (i < n && ((codigo.charAt(i) >= 'A' && codigo.charAt(i) <= 'Z')
                        || (codigo.charAt(i) >= 'a' && codigo.charAt(i) <= 'z')
                        || codigo.charAt(i) == '_')) {
                    int finInvalido = i;
                    while (finInvalido < n && ((codigo.charAt(finInvalido) >= 'A' && codigo.charAt(finInvalido) <= 'Z')
                            || (codigo.charAt(finInvalido) >= 'a' && codigo.charAt(finInvalido) <= 'z')
                            || (codigo.charAt(finInvalido) >= '0' && codigo.charAt(finInvalido) <= '9')
                            || codigo.charAt(finInvalido) == '_')) {
                        finInvalido++;
                    }
                    String invalido = codigo.substring(inicio, finInvalido);
                    agregarError("E02", linea, "Identificador inválido '" + invalido + "'.");
                    tokens.add(new Token(TipoToken.DESCONOCIDO, invalido, linea));
                    i = finInvalido;
                    continue;
                }

                tokens.add(new Token(TipoToken.NUMERO, codigo.substring(inicio, i), linea));
                continue;
            }

            if (c == '"') {
                int inicio = i;
                int lineaInicio = linea;
                i++;
                while (i < n && codigo.charAt(i) != '"' && codigo.charAt(i) != '\n') {
                    i++;
                }
                if (i < n && codigo.charAt(i) == '"') {
                    i++;
                    tokens.add(new Token(TipoToken.CADENA, codigo.substring(inicio, i), lineaInicio));
                } else {
                    agregarError("E02", lineaInicio, "Literal de cadena sin cierre de comillas.");
                }
                continue;
            }

            if (i + 1 < n) {
                String dosChars = codigo.substring(i, i + 2);
                if (dosChars.equals("<=") || dosChars.equals(">=") || dosChars.equals("<>")) {
                    tokens.add(new Token(TipoToken.OPERADOR_RELACIONAL, dosChars, linea));
                    i += 2;
                    continue;
                }
            }

            switch (c) {
                case '=':
                    tokens.add(new Token(TipoToken.OPERADOR_ASIGNACION, "=", linea));
                    break;
                case '<':
                case '>':
                    tokens.add(new Token(TipoToken.OPERADOR_RELACIONAL, String.valueOf(c), linea));
                    break;
                case '+':
                case '-':
                case '*':
                case '/':
                    tokens.add(new Token(TipoToken.OPERADOR_ARITMETICO, String.valueOf(c), linea));
                    break;
                case ';':
                    tokens.add(new Token(TipoToken.PUNTO_Y_COMA, ";", linea));
                    break;
                case ',':
                    tokens.add(new Token(TipoToken.COMA, ",", linea));
                    break;
                case '(':
                    tokens.add(new Token(TipoToken.PARENTESIS_ABRE, "(", linea));
                    break;
                case ')':
                    tokens.add(new Token(TipoToken.PARENTESIS_CIERRA, ")", linea));
                    break;
                default:
                    agregarError("E01", linea, "Carácter no permitido '" + c + "'.");
                    tokens.add(new Token(TipoToken.DESCONOCIDO, String.valueOf(c), linea));
                    break;
            }
            i++;
        }

        tokens.add(new Token(TipoToken.EOF, "EOF", linea));
    }
}