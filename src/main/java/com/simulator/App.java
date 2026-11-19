package com.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class App extends JFrame {

    private JTextArea editorArea;
    private JTextArea lineasArea;
    private JTextArea consolaArea;

    private static final String EJEMPLO_ANEXO_A =
            "Inicio\n" +
            "Declare a, b, c1 Entero;\n" +
            "Declare x Real;\n" +
            "Declare p Cadena;\n" +
            "Declare j, i Entero;\n" +
            "Lea(a); Lea(b);\n" +
            "Si a < 5 Entonces\n" +
            "    a = 3;\n" +
            "    Si b > 1 Entonces\n" +
            "        b = a * (b + (4 - x));\n" +
            "    Sino\n" +
            "        b = b - (a + 2);\n" +
            "    FSi\n" +
            "FSi;\n" +
            "Para i = 1 Hasta j Haga\n" +
            "    c1 = c1 + 1;\n" +
            "    Para b = 1 Hasta i Haga\n" +
            "        x = x + 2 * c1;\n" +
            "    FPara;\n" +
            "FPara;\n" +
            "MQ a < j Haga\n" +
            "    a = a + 1;\n" +
            "    MQ i < a Haga\n" +
            "        i = i + 1;\n" +
            "    FMQ;\n" +
            "FMQ;\n" +
            "Escriba(a);\n" +
            "Escriba(b);\n" +
            "Fin";

    public App() {
        setTitle("Simulador de Compilador - Analizador Sintáctico de Pseudocódigo");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(12, 12, 12, 12));
        panelPrincipal.setBackground(new Color(244, 246, 249));

        JPanel barraBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        barraBotones.setBackground(new Color(244, 246, 249));

        JButton btnEjemplo = crearBoton("Cargar Ejemplo Anexo A", new Color(52, 73, 94));
        JButton btnLimpiar = crearBoton("Limpiar", new Color(127, 140, 141));
        JButton btnAnalizar = crearBoton("ANALIZAR PSEUDOCÓDIGO", new Color(39, 174, 96));

        barraBotones.add(btnEjemplo);
        barraBotones.add(btnLimpiar);
        barraBotones.add(Box.createHorizontalStrut(260));
        barraBotones.add(btnAnalizar);

        editorArea = new JTextArea();
        editorArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        editorArea.setTabSize(4);

        lineasArea = new JTextArea("1");
        lineasArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        lineasArea.setBackground(new Color(225, 228, 232));
        lineasArea.setForeground(new Color(88, 96, 105));
        lineasArea.setEditable(false);
        lineasArea.setMargin(new Insets(0, 8, 0, 8));

        JScrollPane scrollEditor = new JScrollPane(editorArea);
        scrollEditor.setRowHeaderView(lineasArea);

        consolaArea = new JTextArea(11, 40);
        consolaArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        consolaArea.setBackground(new Color(30, 30, 30));
        consolaArea.setForeground(new Color(212, 212, 212));
        consolaArea.setEditable(false);
        consolaArea.setMargin(new Insets(8, 10, 8, 10));

        JScrollPane scrollConsola = new JScrollPane(consolaArea);
        scrollConsola.setBorder(BorderFactory.createTitledBorder("Resultados del Análisis (Consola de Diagnóstico)"));

        editorArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarNumerosLinea(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarNumerosLinea(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarNumerosLinea(); }
        });

        btnEjemplo.addActionListener(e -> {
            editorArea.setText(EJEMPLO_ANEXO_A);
            consolaArea.setText("");
        });
        btnLimpiar.addActionListener(e -> {
            editorArea.setText("");
            consolaArea.setText("");
        });
        btnAnalizar.addActionListener(e -> ejecutarAnalisis());

        panelPrincipal.add(barraBotones, BorderLayout.NORTH);
        panelPrincipal.add(scrollEditor, BorderLayout.CENTER);
        panelPrincipal.add(scrollConsola, BorderLayout.SOUTH);

        add(panelPrincipal);
        editorArea.setText(EJEMPLO_ANEXO_A);
    }

    private JButton crearBoton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private void actualizarNumerosLinea() {
        int totalLineas = editorArea.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= totalLineas; i++) {
            sb.append(i).append("\n");
        }
        lineasArea.setText(sb.toString());
    }

    private void ejecutarAnalisis() {
        String codigo = editorArea.getText();
        AnalizadorLexico lexico = new AnalizadorLexico(codigo);
        lexico.tokenizar();

        AnalizadorSintactico sintactico = new AnalizadorSintactico(lexico.tokens, lexico.errores);
        List<Modelos.ErrorCompilador> erroresTotales = sintactico.analizar();

        erroresTotales.sort(Comparator.comparingInt(e -> e.linea));

        if (erroresTotales.isEmpty()) {
            consolaArea.setForeground(new Color(78, 201, 176));
            consolaArea.setText("[OK] PROGRAMA SINTÁCTICAMENTE CORRECTO.\n" +
                    "Variables declaradas en tabla de símbolos: " + sintactico.tablaSimbolos.size() +
                    " | Tokens analizados: " + lexico.tokens.size());
        } else {
            consolaArea.setForeground(new Color(244, 135, 113));
            StringBuilder sb = new StringBuilder();
            sb.append("[ERROR] SE ENCONTRARON ").append(erroresTotales.size()).append(" ERROR(ES):\n\n");
            for (Modelos.ErrorCompilador err : erroresTotales) {
                sb.append(err.toString()).append("\n");
            }
            consolaArea.setText(sb.toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}