package ec.edu.monster.vista;

/**
 * Menú principal: en la web cada opción es una tarjeta con su enlace (menu.jsp), así que solo
 * queda el saludo al entrar.
 *
 * @author Escobar Marcos
 */
public class MenuVistaWeb extends VistaWeb implements MenuVista {

    @Override
    public void mostrarMenu(String nombre) {
        mostrarMensaje("Bienvenido, " + nombre + ".");
    }

    @Override
    public int leerOpcion() {
        // Cada opción del menú es un enlace; no hay opción que leer.
        return -1;
    }
}
