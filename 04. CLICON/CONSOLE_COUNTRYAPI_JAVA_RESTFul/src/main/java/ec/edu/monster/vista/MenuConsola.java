package ec.edu.monster.vista;

import ec.edu.monster.servicios.SesionServicio;
import java.util.function.BooleanSupplier;

/**
 * Menú principal numerado, en el orden del ECUD1. Al mostrarse indica si el servidor responde
 * (el indicador de los otros clientes).
 *
 * @author Escobar Marcos
 */
public class MenuConsola extends PantallaConsola implements MenuVista {

    public static final int IMPORTAR = 1;
    public static final int CONSULTAR = 2;
    public static final int PAISES = 3;
    public static final int BUSCAR = 4;
    public static final int REPORTES = 5;
    public static final int UNIVERSIDADES = 6;
    public static final int CERRAR_SESION = 7;

    private static final String[] OPCIONES = {
        "Importar países",
        "Consultar país en la API",
        "Países (listar, registrar, editar y eliminar)",
        "Buscar y filtrar",
        "Reportes",
        "Universidades",
        "Cerrar sesión"};

    private final String urlServidor;
    private final BooleanSupplier servidorDisponible;

    public MenuConsola(Consola consola, SesionServicio sesion, String urlServidor, BooleanSupplier servidorDisponible) {
        super(consola, sesion);
        this.urlServidor = urlServidor;
        this.servidorDisponible = servidorDisponible;
    }

    @Override
    public void mostrarMenu(String nombre) {
        consola.mensaje("Bienvenido, " + nombre + ".");
    }

    @Override
    public int leerOpcion() {
        consola.titulo("Menú principal · " + sesion.getNombre());
        consola.ayuda(estadoServidor());
        for (int i = 0; i < OPCIONES.length; i++) {
            consola.texto((i + 1) + ". " + OPCIONES[i]);
        }
        int opcion = consola.leerOpcion("Seleccione una opción");
        if (opcion < IMPORTAR || opcion > CERRAR_SESION) {
            consola.error(OPCION_NO_VALIDA);
        }
        return opcion;
    }

    /** «Servidor: http://... · Servidor disponible» o «... · Servidor no disponible». */
    String estadoServidor() {
        return "Servidor: " + urlServidor + " · "
                + (servidorDisponible.getAsBoolean() ? "Servidor disponible" : "Servidor no disponible");
    }
}
