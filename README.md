# Simulador de Compilador – Analizador Léxico y Sintáctico

Proyecto en Java 17 y Maven con interfaz gráfica en Swing para analizar programas en pseudocódigo según las especificaciones de Autómatas, Gramáticas y Lenguajes.

## Requisitos previos
* **Java JDK 17** (o superior) instalado y configurado en el `PATH`.
* **Apache Maven** instalado (o extensión de Java/Maven en Visual Studio Code).

## Cómo ejecutarlo
1. Clona o descarga este repositorio y abre la carpeta en **Visual Studio Code**.
2. Abre la terminal integrada (`Ctrl + Ñ`) y ejecuta:
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.simulator.App"
   ```
3. También puedes abrir directamente `src/main/java/com/simulator/App.java` y ejecutar el método `main`.
