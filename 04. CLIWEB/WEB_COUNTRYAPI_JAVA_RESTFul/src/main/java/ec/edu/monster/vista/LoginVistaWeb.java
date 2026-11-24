package ec.edu.monster.vista;

import ec.edu.monster.modelos.LoginRequest;

/**
 * Inicio de sesión (ECUD1): lee usuario y clave del formulario de login.jsp.
 *
 * @author Escobar Marcos
 */
public class LoginVistaWeb extends VistaWeb implements LoginVista {

    @Override
    public void mostrarFormulario() {
        // El servlet muestra login.jsp; no hay nada que preparar.
    }

    @Override
    public LoginRequest leerCredenciales() {
        String usuario = parametro("usuario");
        String clave = peticion().getParameter("clave");
        return new LoginRequest(usuario == null ? "" : usuario, clave == null ? "" : clave);
    }

    @Override
    public void limpiarClave() {
        // login.jsp nunca vuelve a escribir la clave en el formulario.
    }
}
