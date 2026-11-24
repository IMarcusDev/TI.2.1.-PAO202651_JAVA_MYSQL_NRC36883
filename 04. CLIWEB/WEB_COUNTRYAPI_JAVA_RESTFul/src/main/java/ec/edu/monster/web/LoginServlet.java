package ec.edu.monster.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Inicio de sesión (ECUD1). GET muestra el formulario; POST pide el token al servidor y, si
 * todo está bien, cambia el id de la sesión HTTP y abre el menú.
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        if (ModulosSesion.conSesion(peticion)) {
            respuesta.sendRedirect(peticion.getContextPath() + "/menu");
            return;
        }
        Pagina.mostrar(peticion, respuesta, "login");
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        modulos.ejecutar(peticion, modulos.getLoginControlador()::iniciarSesion);
        if (!modulos.getSesion().estaActiva()) {
            // Credenciales incorrectas o sin conexión: el formulario vuelve con el usuario escrito.
            Pagina.mostrar(peticion, respuesta, "login");
            return;
        }
        // Nuevo id de sesión al autenticarse (evita la fijación de sesión).
        peticion.changeSessionId();
        Pagina.redirigir(peticion, respuesta, "/menu");
    }
}
