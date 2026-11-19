package com.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.simulator.Modelos.ErrorCompilador;
import com.simulator.Modelos.TipoError;
import com.simulator.Modelos.TipoToken;
import com.simulator.Modelos.Token;

public class AnalizadorSintactico {

    private final List<Token> tokens;
    private final List<ErrorCompilador> errores;
    public final Map<String, String> tablaSimbolos = new HashMap<>();
    private int pos = 0;

    public AnalizadorSintactico(List<Token> tokens, List<ErrorCompilador> erroresPrevios) {
        this.tokens = tokens;
        this.errores = new ArrayList<>(erroresPrevios);
    }

    private Token actual() {
        return pos < tokens.size() ? tokens.get(pos) : tokens.get(tokens.size() - 1);
    }

    private Token anterior() {
        return pos > 0 ? tokens.get(pos - 1) : tokens.get(0);
    }

    private void avanzar() {
        if (pos < tokens.size() - 1) {
            pos++;
        }
    }

    private String norm(Token t) {
        return t.lexema.toLowerCase(Locale.ROOT);
    }

    private boolean esPalabra(String palabra) {
        return actual().tipo == TipoToken.PALABRA_CLAVE && norm(actual()).equals(palabra.toLowerCase(Locale.ROOT));
    }

    private boolean coincidirPalabra(String palabra) {
        if (esPalabra(palabra)) {
            avanzar();
            return true;
        }
        return false;
    }

    private void registrarError(String codigo, int linea, String detalleExtra) {
        TipoError tipo = DefinicionesLenguaje.tipoDeError(codigo);
        String base = DefinicionesLenguaje.obtenerDescripcionError(codigo);
        String msg = (detalleExtra == null || detalleExtra.isEmpty()) ? base : base + " " + detalleExtra;
        errores.add(new ErrorCompilador(codigo, tipo, linea, msg));
    }

    public List<ErrorCompilador> analizar() {
        if (actual().tipo == TipoToken.EOF) {
            registrarError("E03", 1, "");
            return errores;
        }

        if (!coincidirPalabra("Inicio")) {
            registrarError("E03", actual().linea, "");
        }

        while (esPalabra("Declare")) {
            parsearDeclaracion();
        }

        parsearBloque(new String[]{"Fin"}, false);

        if (!coincidirPalabra("Fin")) {
            registrarError("E04", actual().linea, "");
        } else if (actual().tipo != TipoToken.EOF) {
            registrarError("E04", actual().linea, "Token encontrado después de 'Fin': '" + actual().lexema + "'.");
        }

        return errores;
    }

    private void parsearBloque(String[] delimitadoresCierre, boolean permitirSino) {
        boolean vioSinoEnEsteBloque = false;

        while (actual().tipo != TipoToken.EOF && !esDelimitadorCierre(delimitadoresCierre)) {
            Token t = actual();

            if (t.tipo == TipoToken.PALABRA_CLAVE) {
                String p = norm(t);
                switch (p) {
                    case "declare":
                        registrarError("E06", t.linea, "Las declaraciones deben ir al inicio del programa.");
                        parsearDeclaracion();
                        break;
                    case "lea":
                        parsearLea();
                        break;
                    case "escriba":
                        parsearEscriba();
                        break;
                    case "si":
                        parsearSi();
                        break;
                    case "para":
                        parsearPara();
                        break;
                    case "mq":
                        parsearMQ();
                        break;
                    case "sino":
                        if (permitirSino && !vioSinoEnEsteBloque) {
                            vioSinoEnEsteBloque = true;
                            avanzar();
                        } else {
                            registrarError("E11", t.linea, "'Sino' sin bloque 'Si' abierto o duplicado.");
                            avanzar();
                        }
                        break;
                    case "fsi":
                    case "fpara":
                    case "fmq":
                        registrarError("E11", t.linea, "Cierre '" + t.lexema + "' sin bloque abierto correspondiente.");
                        avanzar();
                        if (actual().tipo == TipoToken.PUNTO_Y_COMA) {
                            avanzar();
                        }
                        break;
                    default:
                        registrarError("E07", t.linea, "Palabra reservada inesperada en sentencia: '" + t.lexema + "'.");
                        avanzar();
                        break;
                }
            } else if (t.tipo == TipoToken.IDENTIFICADOR) {
                parsearAsignacion();
            } else if (t.tipo == TipoToken.DESCONOCIDO) {
                avanzar();
            } else {
                registrarError("E07", t.linea, "Token inesperado: '" + t.lexema + "'.");
                avanzar();
            }
        }
    }

    private boolean esDelimitadorCierre(String[] delimitadores) {
        if (actual().tipo != TipoToken.PALABRA_CLAVE) {
            return false;
        }
        String p = norm(actual());
        for (String d : delimitadores) {
            if (p.equals(d.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void exigirPuntoYComa(int lineaReferencia) {
        if (actual().tipo == TipoToken.PUNTO_Y_COMA) {
            avanzar();
        } else {
            registrarError("E05", lineaReferencia, "");
        }
    }

    private void parsearDeclaracion() {
        int linea = actual().linea;
        avanzar();

        List<Token> vars = new ArrayList<>();
        boolean errorLista = false;

        if (actual().tipo == TipoToken.COMA) {
            registrarError("E06", actual().linea, "No se admite ',' al inicio de la lista.");
            errorLista = true;
            avanzar();
        }

        if (actual().tipo == TipoToken.IDENTIFICADOR) {
            vars.add(actual());
            avanzar();
            while (actual().tipo == TipoToken.COMA) {
                avanzar();
                if (actual().tipo == TipoToken.COMA) {
                    registrarError("E06", actual().linea, "No se admiten dos comas seguidas.");
                    errorLista = true;
                    avanzar();
                } else if (actual().tipo == TipoToken.IDENTIFICADOR) {
                    vars.add(actual());
                    avanzar();
                } else {
                    registrarError("E06", actual().linea, "Se esperaba un identificador después de ','.");
                    errorLista = true;
                    break;
                }
            }
        } else if (!errorLista) {
            registrarError("E06", linea, "Se esperaba al menos un identificador en 'Declare'.");
        }

        String tipoDato = null;
        if (actual().tipo == TipoToken.TIPO_DATO) {
            tipoDato = DefinicionesLenguaje.normalizarTipo(actual().lexema);
            avanzar();
        } else {
            registrarError("E06", actual().linea, "Tipo de dato faltante o inválido.");
            if (actual().tipo == TipoToken.IDENTIFICADOR) {
                avanzar();
            }
        }

        if (tipoDato != null) {
            for (Token v : vars) {
                String clave = v.lexema.toLowerCase(Locale.ROOT);
                if (tablaSimbolos.containsKey(clave)) {
                    registrarError("E15", v.linea, "Variable '" + v.lexema + "' ya declarada.");
                } else {
                    tablaSimbolos.put(clave, tipoDato);
                }
            }
        }

        exigirPuntoYComa(anterior().linea);
    }

    private void parsearLea() {
        int linea = actual().linea;
        avanzar();

        if (actual().tipo != TipoToken.PARENTESIS_ABRE) {
            registrarError("E07", linea, "Se esperaba '(' después de 'Lea'.");
        } else {
            avanzar();
        }

        if (actual().tipo == TipoToken.IDENTIFICADOR) {
            String nombre = actual().lexema;
            if (!tablaSimbolos.containsKey(nombre.toLowerCase(Locale.ROOT))) {
                registrarError("E14", actual().linea, "Variable '" + nombre + "' no declarada.");
            }
            avanzar();
        } else {
            registrarError("E07", actual().linea, "Se esperaba un identificador dentro de 'Lea(...)'.");
        }

        if (actual().tipo != TipoToken.PARENTESIS_CIERRA) {
            registrarError("E07", anterior().linea, "Se esperaba ')' en 'Lea'.");
        } else {
            avanzar();
        }

        exigirPuntoYComa(anterior().linea);
    }

    private void parsearEscriba() {
        int linea = actual().linea;
        avanzar();

        if (actual().tipo != TipoToken.PARENTESIS_ABRE) {
            registrarError("E07", linea, "Se esperaba '(' después de 'Escriba'.");
        } else {
            avanzar();
        }

        if (actual().tipo == TipoToken.CADENA) {
            avanzar();
        } else {
            parsearExpresion();
        }

        if (actual().tipo != TipoToken.PARENTESIS_CIERRA) {
            registrarError("E07", anterior().linea, "Se esperaba ')' en 'Escriba'.");
        } else {
            avanzar();
        }

        exigirPuntoYComa(anterior().linea);
    }

    private void parsearAsignacion() {
        Token varToken = actual();
        String clave = varToken.lexema.toLowerCase(Locale.ROOT);
        if (!tablaSimbolos.containsKey(clave)) {
            registrarError("E14", varToken.linea, "Variable '" + varToken.lexema + "' no declarada.");
        }
        avanzar();

        if (actual().tipo == TipoToken.OPERADOR_ASIGNACION) {
            avanzar();
        } else {
            registrarError("E07", actual().linea, "Falta '=' en la asignación.");
        }

        if (actual().tipo == TipoToken.CADENA) {
            avanzar();
        } else {
            parsearExpresion();
        }

        while (actual().tipo == TipoToken.PARENTESIS_CIERRA) {
            registrarError("E12", actual().linea, "Paréntesis ')' sin apertura correspondiente.");
            avanzar();
        }

        exigirPuntoYComa(anterior().linea);
    }

    private void parsearSi() {
        int lineaApertura = actual().linea;
        avanzar();

        parsearCondicion();

        if (!coincidirPalabra("Entonces")) {
            registrarError("E09", lineaApertura, "Falta 'Entonces' en el bloque 'Si'.");
        }

        parsearBloque(new String[]{"Sino", "FSi", "Fin"}, false);

        if (coincidirPalabra("Sino")) {
            parsearBloque(new String[]{"Sino", "FSi", "Fin"}, true);
            if (esPalabra("Sino")) {
                registrarError("E11", actual().linea, "'Sino' duplicado en el mismo bloque 'Si'.");
                avanzar();
                parsearBloque(new String[]{"FSi", "Fin"}, false);
            }
        }

        if (!coincidirPalabra("FSi")) {
            registrarError("E10", lineaApertura, "Bloque 'Si' sin cierre 'FSi'.");
        } else if (actual().tipo == TipoToken.PUNTO_Y_COMA) {
            avanzar();
        }
    }

    private void parsearPara() {
        int lineaApertura = actual().linea;
        avanzar();

        if (actual().tipo == TipoToken.IDENTIFICADOR) {
            Token var = actual();
            String tipoVar = tablaSimbolos.get(var.lexema.toLowerCase(Locale.ROOT));
            if (tipoVar == null) {
                registrarError("E14", var.linea, "Variable '" + var.lexema + "' no declarada.");
            } else if (!tipoVar.equals("Entero")) {
                registrarError("E16", var.linea, "La variable '" + var.lexema + "' del 'Para' debe ser Entero.");
            }
            avanzar();
        } else {
            registrarError("E09", lineaApertura, "Se esperaba variable de control en 'Para'.");
        }

        if (actual().tipo == TipoToken.OPERADOR_ASIGNACION) {
            avanzar();
        } else {
            registrarError("E09", lineaApertura, "Falta '=' en 'Para'.");
        }

        validarIndicePara(lineaApertura);

        if (!coincidirPalabra("Hasta")) {
            registrarError("E09", lineaApertura, "Falta 'Hasta' en 'Para'.");
        }

        validarIndicePara(lineaApertura);

        if (!coincidirPalabra("Haga")) {
            registrarError("E09", lineaApertura, "Falta 'Haga' en 'Para'.");
        }

        parsearBloque(new String[]{"FPara", "Fin"}, false);

        if (!coincidirPalabra("FPara")) {
            registrarError("E10", lineaApertura, "Bloque 'Para' sin cierre 'FPara'.");
        } else if (actual().tipo == TipoToken.PUNTO_Y_COMA) {
            avanzar();
        }
    }

    private void validarIndicePara(int lineaBloque) {
        Token t = actual();
        if (t.tipo == TipoToken.NUMERO) {
            if (t.lexema.contains(".")) {
                registrarError("E16", t.linea, "El índice '" + t.lexema + "' del 'Para' debe ser un número entero.");
            }
            avanzar();
        } else if (t.tipo == TipoToken.IDENTIFICADOR) {
            String tipo = tablaSimbolos.get(t.lexema.toLowerCase(Locale.ROOT));
            if (tipo == null) {
                registrarError("E14", t.linea, "Variable '" + t.lexema + "' no declarada.");
            } else if (!tipo.equals("Entero")) {
                registrarError("E16", t.linea, "La variable '" + t.lexema + "' del 'Para' debe ser Entero.");
            }
            avanzar();
        } else {
            registrarError("E09", lineaBloque, "Se esperaba un índice válido en 'Para'.");
        }
    }

    private void parsearMQ() {
        int lineaApertura = actual().linea;
        avanzar();

        parsearCondicion();

        if (!coincidirPalabra("Haga")) {
            registrarError("E09", lineaApertura, "Falta 'Haga' en el bloque 'MQ'.");
        }

        parsearBloque(new String[]{"FMQ", "Fin"}, false);

        if (!coincidirPalabra("FMQ")) {
            registrarError("E10", lineaApertura, "Bloque 'MQ' sin cierre 'FMQ'.");
        } else if (actual().tipo == TipoToken.PUNTO_Y_COMA) {
            avanzar();
        }
    }

    private void parsearCondicion() {
        int lineaCond = actual().linea;
        boolean izqOk = parsearOperandoCondicion(lineaCond);

        if (actual().tipo == TipoToken.OPERADOR_RELACIONAL) {
            avanzar();
        } else {
            registrarError("E08", lineaCond, "Operador relacional faltante o no válido.");
            return;
        }

        boolean derOk = parsearOperandoCondicion(lineaCond);
        if (!izqOk || !derOk) {
            registrarError("E08", lineaCond, "Condición incompleta.");
        }
    }

    private boolean parsearOperandoCondicion(int lineaFallback) {
        Token t = actual();
        if (t.tipo == TipoToken.IDENTIFICADOR) {
            if (!tablaSimbolos.containsKey(t.lexema.toLowerCase(Locale.ROOT))) {
                registrarError("E14", t.linea, "Variable '" + t.lexema + "' no declarada.");
            }
            avanzar();
            return true;
        } else if (t.tipo == TipoToken.NUMERO) {
            avanzar();
            return true;
        } else {
            registrarError("E08", lineaFallback, "Se esperaba identificador o número en la condición.");
            return false;
        }
    }

    private void parsearExpresion() {
        int balanceParentesis = 0;
        boolean esperaOperando = true;
        int lineaExpr = actual().linea;
        boolean huboOperador = false;

        while (actual().tipo != TipoToken.EOF) {
            Token t = actual();

            if (esperaOperando) {
                if (t.tipo == TipoToken.PARENTESIS_ABRE) {
                    balanceParentesis++;
                    avanzar();
                } else if (t.tipo == TipoToken.IDENTIFICADOR) {
                    if (!tablaSimbolos.containsKey(t.lexema.toLowerCase(Locale.ROOT))) {
                        registrarError("E14", t.linea, "Variable '" + t.lexema + "' no declarada.");
                    }
                    esperaOperando = false;
                    avanzar();
                } else if (t.tipo == TipoToken.NUMERO) {
                    esperaOperando = false;
                    avanzar();
                } else if (t.tipo == TipoToken.PARENTESIS_CIERRA) {
                    if (balanceParentesis > 0) {
                        registrarError("E13", t.linea, "Expresión inválida dentro de paréntesis.");
                        balanceParentesis--;
                        avanzar();
                        esperaOperando = false;
                    } else {
                        break;
                    }
                } else {
                    if (huboOperador || t.tipo == TipoToken.OPERADOR_ARITMETICO) {
                        registrarError("E13", t.linea, "Falta operando en la expresión.");
                    }
                    break;
                }
            } else {
                if (t.tipo == TipoToken.PARENTESIS_CIERRA) {
                    if (balanceParentesis > 0) {
                        balanceParentesis--;
                        avanzar();
                    } else {
                        break;
                    }
                } else if (t.tipo == TipoToken.OPERADOR_ARITMETICO) {
                    huboOperador = true;
                    esperaOperando = true;
                    avanzar();
                } else if (t.tipo == TipoToken.PARENTESIS_ABRE) {
                    registrarError("E13", t.linea, "Falta operador antes de '('.");
                    balanceParentesis++;
                    avanzar();
                    esperaOperando = true;
                } else {
                    break;
                }
            }
        }

        if (balanceParentesis > 0) {
            registrarError("E12", lineaExpr, "Faltan " + balanceParentesis + " paréntesis de cierre ')'.");
        }
        if (esperaOperando && huboOperador) {
            registrarError("E13", anterior().linea, "Falta operando después del operador.");
        }
    }
}