package ec.edu.monster.vista;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Entrada y salida de texto de todas las pantallas: títulos, mensajes, errores con el prefijo
 * «ERROR:», tablas y lectura de datos. Recibe la entrada y la salida por constructor para que
 * las pruebas puedan escribir las respuestas del usuario y leer lo que se mostró.
 *
 * <p>Los colores del diseño de referencia (azul para títulos, verde para mensajes y rojo para
 * errores) se usan solo en terminales que entienden ANSI; el texto es el mismo con o sin color.</p>
 *
 * @author Escobar Marcos
 */
public class Consola {

    /** El usuario eligió salir o la entrada se terminó (fin de archivo). */
    public static final class Salida extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public Salida() {
            super("Fin de la entrada", null, false, false);
        }
    }

    static final int ANCHO = 100;
    private static final String AZUL = "\u001B[1;34m";
    private static final String VERDE = "\u001B[32m";
    private static final String ROJO = "\u001B[1;31m";
    private static final String TENUE = "\u001B[2m";
    private static final String NORMAL = "\u001B[0m";

    private final BufferedReader entrada;
    private final PrintStream salida;
    private final boolean colores;
    private final Console terminal;

    /** Consola de pruebas (o de cualquier flujo): sin colores y sin ocultar la clave. */
    public Consola(BufferedReader entrada, PrintStream salida) {
        this(entrada, salida, false, null);
    }

    private Consola(BufferedReader entrada, PrintStream salida, boolean colores, Console terminal) {
        this.entrada = entrada;
        this.salida = salida;
        this.colores = colores;
        this.terminal = terminal;
    }

    /**
     * Consola del sistema en UTF-8 (las tildes de los países y mensajes se ven bien también en
     * Windows) y con colores si la terminal los soporta.
     */
    public static Consola delSistema() {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        if (windows && System.console() != null) {
            try {
                new ProcessBuilder("cmd", "/c", "chcp", "65001")
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start()
                        .waitFor();
            } catch (IOException e) {
                // Sin chcp algunas tildes se verán mal, pero el programa funciona.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        PrintStream salida = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Console terminal = esTerminal(System.console()) ? System.console() : null;
        // Windows Terminal (WT_SESSION), Git Bash y las terminales tipo Unix (TERM) entienden
        // ANSI; la consola clásica de Windows (cmd sin Windows Terminal) no siempre.
        // La variable NO_COLOR los desactiva.
        boolean colores = terminal != null && System.getenv("NO_COLOR") == null
                && (System.getenv("WT_SESSION") != null || System.getenv("TERM") != null
                    || System.getenv("TERM_PROGRAM") != null);
        return new Consola(entrada, salida, colores, terminal);
    }

    // ------------------------------------------------------------------ salida

    /** Título de pantalla entre líneas, como la barra azul de los otros clientes. */
    public void titulo(String texto) {
        salida.println();
        salida.println(color(AZUL, "=".repeat(ANCHO)));
        salida.println(color(AZUL, " " + texto));
        salida.println(color(AZUL, "=".repeat(ANCHO)));
    }

    /** Subtítulo de una sección dentro de la pantalla. */
    public void subtitulo(String texto) {
        salida.println();
        salida.println(color(AZUL, texto));
        salida.println(color(AZUL, "-".repeat(texto.length())));
    }

    /** Mensaje de éxito (verde). */
    public void mensaje(String texto) {
        salida.println(color(VERDE, texto));
    }

    /** Error (rojo), siempre con el prefijo «ERROR:». */
    public void error(String texto) {
        salida.println(color(ROJO, "ERROR: " + texto));
    }

    /** Texto normal. */
    public void texto(String texto) {
        salida.println(texto);
    }

    /** Texto secundario (ayudas, «Procesando...»). */
    public void ayuda(String texto) {
        salida.println(color(TENUE, texto));
    }

    public void lineaEnBlanco() {
        salida.println();
    }

    public void tabla(Tabla tabla) {
        salida.print(tabla.texto());
    }

    // ------------------------------------------------------------------ entrada

    /** Lee una línea (sin espacios a los lados). Si la entrada se termina, sale del programa. */
    public String leer(String etiqueta) {
        salida.print(etiqueta + ": ");
        salida.flush();
        try {
            String linea = entrada.readLine();
            if (linea == null) {
                salida.println();
                throw new Salida();
            }
            return linea.trim();
        } catch (IOException e) {
            throw new Salida();
        }
    }

    /** Lee la clave sin mostrarla cuando hay una terminal real. */
    public String leerClave(String etiqueta) {
        if (terminal == null) {
            return leer(etiqueta);
        }
        char[] clave = terminal.readPassword("%s: ", etiqueta);
        if (clave == null) {
            throw new Salida();
        }
        return new String(clave).trim();
    }

    /** Pregunta de sí o no: «S», «Si» o «Sí» cuentan como sí. */
    public boolean confirmar(String pregunta) {
        String respuesta = leer(pregunta + " (S/N)").toLowerCase(Locale.ROOT);
        return respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí");
    }

    /** Lee un número de opción; devuelve -1 si no es un número. */
    public int leerOpcion(String etiqueta) {
        try {
            return Integer.parseInt(leer(etiqueta));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String color(String codigo, String texto) {
        return colores ? codigo + texto + NORMAL : texto;
    }

    /** Desde Java 22, System.console() existe aunque la entrada venga de un archivo; isTerminal() lo distingue. */
    private static boolean esTerminal(Console consola) {
        if (consola == null) {
            return false;
        }
        try {
            return (Boolean) Console.class.getMethod("isTerminal").invoke(consola);
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }
}
