package ec.edu.monster.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Cierre de sesión (ECUD1): avisa al servidor para que elimine el token, cierra la sesión HTTP
 * y vuelve al inicio de sesión, aunque el servidor no responda.
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        ModulosSesion modulos = ModulosSesion.existente(peticion);
        if (modulos != null) {
            modulos.ejecutar(peticion, modulos.getLoginControlador()::cerrarSesion);
        }
        Pagina.alInicioSesion(peticion, respuesta);
    }
}
