package ec.edu.monster.vista;

/**
 * Lo que toda vista sabe hacer, en cualquier tecnología (Swing, consola, JSP o Android).
 * Los controladores solo conocen estas interfaces, por eso se prueban sin pantalla.
 *
 * @author Escobar Marcos
 */
public interface VistaBase {

    /** Muestra un mensaje de éxito o informativo (alerta verde). */
    void mostrarMensaje(String mensaje);

    /** Muestra un mensaje de error destacado (alerta roja). */
    void mostrarError(String mensaje);

    /** Vuelve al formulario de inicio de sesión (sesión cerrada o expirada). */
    void irAInicioSesion();
}
