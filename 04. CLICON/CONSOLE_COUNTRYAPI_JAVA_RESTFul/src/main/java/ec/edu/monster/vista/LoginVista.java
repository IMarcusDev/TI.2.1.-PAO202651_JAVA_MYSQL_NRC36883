package ec.edu.monster.vista;

import ec.edu.monster.modelos.LoginRequest;

/**
 * Formulario de inicio de sesión (ECUD1).
 *
 * @author Escobar Marcos
 */
public interface LoginVista extends VistaBase {

    /** Muestra el formulario vacío, listo para escribir. */
    void mostrarFormulario();

    /** Usuario y clave escritos (pueden venir vacíos; los valida el controlador). */
    LoginRequest leerCredenciales();

    /** Borra la clave después de unas credenciales incorrectas. */
    void limpiarClave();
}
