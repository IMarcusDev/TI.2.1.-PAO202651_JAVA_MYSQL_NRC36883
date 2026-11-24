package ec.edu.monster.vista;

/**
 * Menú principal: Importar países, Consultar país en la API, Países, Buscar y filtrar,
 * Reportes, Universidades y Cerrar sesión.
 *
 * @author Escobar Marcos
 */
public interface MenuVista extends VistaBase {

    /** Muestra el menú con el nombre del usuario de la sesión. */
    void mostrarMenu(String nombre);

    /** Opción elegida (la usa la consola; en las vistas gráficas cada opción es un botón). */
    int leerOpcion();
}
