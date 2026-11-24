package ec.edu.monster.vista;

import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.servicios.SesionServicio;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pantalla de texto (equivale al PanelBase de escritorio): implementa la parte común de
 * {@link VistaBase} y ejecuta las acciones del controlador sin que un error inesperado cierre
 * el programa.
 *
 * @author Escobar Marcos
 */
public abstract class PantallaConsola implements VistaBase {

    static final String OPCION_NO_VALIDA = "Opción no válida";
    static final String ERROR_INESPERADO = ClienteRest.RESPUESTA_NO_VALIDA;

    private static final Logger LOG = Logger.getLogger(PantallaConsola.class.getName());

    protected final Consola consola;
    protected final SesionServicio sesion;

    protected PantallaConsola(Consola consola, SesionServicio sesion) {
        this.consola = consola;
        this.sesion = sesion;
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        consola.mensaje(mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        consola.error(mensaje);
    }

    /**
     * El controlador ya cerró la sesión: los ciclos de las pantallas terminan porque
     * {@code sesion.estaActiva()} es falso y App vuelve al inicio de sesión.
     */
    @Override
    public void irAInicioSesion() {
        // Nada que hacer aquí: el regreso lo hace el ciclo principal de App.
    }

    /** Ejecuta una acción del controlador; un error inesperado se muestra y el programa sigue. */
    protected void ejecutar(Runnable accion) {
        ejecutar(consola, accion);
    }

    /** Igual que {@link #ejecutar(Runnable)}, para quien no es una pantalla (App). */
    public static void ejecutar(Consola consola, Runnable accion) {
        try {
            accion.run();
        } catch (Consola.Salida e) {
            throw e;
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Error inesperado en una acción", e);
            consola.error(ERROR_INESPERADO);
        }
    }
}
