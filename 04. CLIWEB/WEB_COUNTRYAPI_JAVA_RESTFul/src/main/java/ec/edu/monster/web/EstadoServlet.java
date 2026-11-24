package ec.edu.monster.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Estado del servidor RESTful para el indicador de la barra superior: responde
 * {@code {"disponible":true}} o {@code false}. La página lo consulta cada 10 segundos.
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "EstadoServlet", urlPatterns = "/estado")
public class EstadoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        boolean disponible = AplicacionWeb.cliente(getServletContext()).servidorDisponible();
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.setHeader("Cache-Control", "no-store");
        respuesta.getWriter().write("{\"disponible\":" + disponible + "}");
    }
}
