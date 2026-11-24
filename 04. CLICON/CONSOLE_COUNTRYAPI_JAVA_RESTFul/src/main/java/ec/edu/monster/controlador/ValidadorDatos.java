package ec.edu.monster.controlador;

import ec.edu.monster.modelos.Pais;
import java.util.regex.Pattern;

/**
 * Validaciones de los datos que escribe el usuario, iguales a las del ECUD. Cada método devuelve
 * el mensaje de error que se debe mostrar, o null si los datos son válidos; así el cliente no
 * envía peticiones con datos incompletos.
 *
 * @author Escobar Marcos
 */
public final class ValidadorDatos {

    public static final String INGRESE_USUARIO = "Ingrese el usuario";
    public static final String INGRESE_CLAVE = "Ingrese la clave";
    public static final String NOMBRE_CORTO = "Ingrese al menos dos caracteres";
    public static final String NOMBRE_PAIS = "Ingrese el nombre del país";
    public static final String SELECCIONE_REGION = "Seleccione una región";
    public static final String POBLACION = "La población debe ser un número entero mayor o igual que cero";
    public static final String AREA = "El área debe ser un número mayor o igual que cero";
    public static final String TEXTOS_LARGOS = "La capital, la subregión y la moneda deben tener como máximo 100 caracteres";
    public static final String INGRESE_PAIS = "Ingrese el país";

    private static final int LARGO_MAXIMO = 100;
    /** Entero con separadores de miles («17.643.060», «17,643,060» o «17 643 060»). */
    private static final Pattern ENTERO_CON_MILES = Pattern.compile("\\d{1,3}([.,\\s]\\d{3})+");
    private static final Pattern ENTERO = Pattern.compile("\\d+");
    /** Número con una parte decimal opcional, con punto o coma («276841», «2,5»). */
    private static final Pattern DECIMAL = Pattern.compile("\\d+([.,]\\d+)?");

    private ValidadorDatos() {
    }

    /** Usuario y clave no vacíos (ECUD1, flujo alternativo 2). */
    public static String validarCredenciales(String usuario, String clave) {
        if (usuario == null || usuario.isBlank()) {
            return INGRESE_USUARIO;
        }
        if (clave == null || clave.isEmpty()) {
            return INGRESE_CLAVE;
        }
        return null;
    }

    /** Al menos dos caracteres para consultar en la API (ECUD3, flujo alternativo 3). */
    public static String validarNombreConsulta(String nombre) {
        return nombre == null || nombre.trim().length() < 2 ? NOMBRE_CORTO : null;
    }

    /** Datos de un país antes de registrarlo o actualizarlo (ECUD4, flujo alternativo 2). */
    public static String validarPais(Pais pais) {
        if (pais == null || pais.getNombre() == null || pais.getNombre().isBlank()
                || pais.getNombre().trim().length() > LARGO_MAXIMO) {
            return NOMBRE_PAIS;
        }
        if (pais.getRegion() == null || !Pais.REGIONES.contains(pais.getRegion())) {
            return SELECCIONE_REGION;
        }
        if (pais.getPoblacion() < 0) {
            return POBLACION;
        }
        if (pais.getArea() < 0 || Double.isNaN(pais.getArea()) || Double.isInfinite(pais.getArea())) {
            return AREA;
        }
        if (muyLargo(pais.getCapital()) || muyLargo(pais.getSubregion()) || muyLargo(pais.getMoneda())) {
            return TEXTOS_LARGOS;
        }
        return null;
    }

    /** País para importar o listar universidades, no vacío (ECUD10, flujo alternativo 2). */
    public static String validarPaisUniversidades(String pais) {
        return pais == null || pais.isBlank() ? INGRESE_PAIS : null;
    }

    /**
     * Convierte el texto de la población. Acepta separadores de miles. Si el texto no es un
     * entero mayor o igual que cero devuelve -1, que {@link #validarPais} rechaza.
     */
    public static long leerPoblacion(String texto) {
        if (texto == null) {
            return -1;
        }
        String limpio = texto.trim();
        if (ENTERO_CON_MILES.matcher(limpio).matches()) {
            limpio = limpio.replaceAll("[.,\\s]", "");
        }
        if (!ENTERO.matcher(limpio).matches()) {
            return -1;
        }
        try {
            return Long.parseLong(limpio);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Convierte el texto del área (km²), con punto o coma decimal. Si no es un número mayor o
     * igual que cero devuelve NaN, que {@link #validarPais} rechaza. Vacío equivale a 0.
     */
    public static double leerArea(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        String limpio = texto.trim();
        if (!DECIMAL.matcher(limpio).matches()) {
            return Double.NaN;
        }
        return Double.parseDouble(limpio.replace(',', '.'));
    }

    private static boolean muyLargo(String texto) {
        return texto != null && texto.trim().length() > LARGO_MAXIMO;
    }
}
