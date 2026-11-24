package ec.edu.monster.vista;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tabla de texto con bordes, como la JTable de escritorio:
 *
 * <pre>
 * +----+---------+-----------+
 * | ID | Nombre  | Población |
 * +----+---------+-----------+
 * |  1 | Ecuador | 17.643.060|
 * +----+---------+-----------+
 * </pre>
 *
 * Las columnas numéricas van a la derecha. Un texto más largo que el ancho máximo de su
 * columna se corta con «…» para que la tabla no se desborde.
 *
 * @author Escobar Marcos
 */
public class Tabla {

    private static final Locale ECUADOR = Locale.forLanguageTag("es-EC");

    private final String[] encabezados;
    private final int[] maximos;
    private final boolean[] derecha;
    private final List<String[]> filas = new ArrayList<>();

    public Tabla(String... encabezados) {
        this.encabezados = encabezados;
        this.maximos = new int[encabezados.length];
        this.derecha = new boolean[encabezados.length];
        java.util.Arrays.fill(maximos, Integer.MAX_VALUE);
    }

    /** Alinea a la derecha las columnas indicadas (números). */
    public Tabla derecha(int... columnas) {
        for (int columna : columnas) {
            derecha[columna] = true;
        }
        return this;
    }

    /** Ancho máximo de una columna. */
    public Tabla maximo(int columna, int ancho) {
        maximos[columna] = Math.max(3, ancho);
        return this;
    }

    public Tabla fila(Object... celdas) {
        String[] texto = new String[encabezados.length];
        for (int i = 0; i < texto.length; i++) {
            Object celda = i < celdas.length ? celdas[i] : null;
            texto[i] = celda == null ? "" : celda.toString();
        }
        filas.add(texto);
        return this;
    }

    public int cantidadFilas() {
        return filas.size();
    }

    /** La tabla completa, con salto de línea al final. */
    public String texto() {
        int[] anchos = new int[encabezados.length];
        for (int i = 0; i < anchos.length; i++) {
            anchos[i] = Math.min(maximos[i], encabezados[i].length());
            for (String[] fila : filas) {
                anchos[i] = Math.max(anchos[i], Math.min(maximos[i], fila[i].length()));
            }
        }
        StringBuilder texto = new StringBuilder();
        String borde = borde(anchos);
        texto.append(borde).append(linea(encabezados, anchos, new boolean[anchos.length])).append(borde);
        for (String[] fila : filas) {
            texto.append(linea(fila, anchos, derecha));
        }
        if (!filas.isEmpty()) {
            texto.append(borde);
        }
        return texto.toString();
    }

    /** Entero con separador de miles de Ecuador, por ejemplo 17.643.060. */
    public static String entero(long valor) {
        return NumberFormat.getIntegerInstance(ECUADOR).format(valor);
    }

    /** Número con separador de miles y hasta dos decimales. */
    public static String decimal(double valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(ECUADOR);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

    private static String borde(int[] anchos) {
        StringBuilder linea = new StringBuilder("+");
        for (int ancho : anchos) {
            linea.append("-".repeat(ancho + 2)).append('+');
        }
        return linea.append(System.lineSeparator()).toString();
    }

    private static String linea(String[] celdas, int[] anchos, boolean[] derecha) {
        StringBuilder linea = new StringBuilder("|");
        for (int i = 0; i < anchos.length; i++) {
            String celda = cortar(celdas[i], anchos[i]);
            String relleno = " ".repeat(anchos[i] - celda.length());
            linea.append(' ').append(derecha[i] ? relleno + celda : celda + relleno).append(" |");
        }
        return linea.append(System.lineSeparator()).toString();
    }

    private static String cortar(String texto, int ancho) {
        return texto.length() <= ancho ? texto : texto.substring(0, ancho - 1) + "…";
    }
}
