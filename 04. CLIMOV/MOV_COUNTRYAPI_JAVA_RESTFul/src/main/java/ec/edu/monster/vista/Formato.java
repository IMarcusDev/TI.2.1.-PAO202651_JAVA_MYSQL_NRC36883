package ec.edu.monster.vista;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formato de números como en los demás clientes: separador de miles de Ecuador (17.643.060).
 *
 * @author Escobar Marcos
 */
final class Formato {

    private static final Locale ECUADOR = new Locale("es", "EC");

    private Formato() {
    }

    /** Entero con separador de miles, por ejemplo 17.643.060. */
    static String entero(long valor) {
        return NumberFormat.getIntegerInstance(ECUADOR).format(valor);
    }

    /** Número con separador de miles y hasta dos decimales. */
    static String decimal(double valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(ECUADOR);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

    /** Número sin separadores para llenar un campo editable (276841.5 → «276841,5»). */
    static String editable(double valor) {
        if (valor == Math.rint(valor)) {
            return Long.toString((long) valor);
        }
        return Double.toString(valor).replace('.', ',');
    }

    /** Texto o cadena vacía si es null. */
    static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    /** Texto recortado, o null si está vacío. */
    static String vacioANulo(CharSequence valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.toString().trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
