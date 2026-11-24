package ec.edu.monster.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * «Buscar y filtrar» (ECUD5). Cada GET busca con los criterios de la dirección (?nombre,
 * ?capital, ?region); sin criterios muestra todos, como al entrar en escritorio.
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "BuscarServlet", urlPatterns = "/buscar")
public class BuscarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        modulos.ejecutar(peticion, modulos.getPaisControlador()::buscarYFiltrar);
        if (Pagina.sesionExpirada(modulos, peticion, respuesta)) {
            return;
        }
        peticion.setAttribute("regiones", modulos.getBusqueda().getRegiones());
        Pagina.mostrar(peticion, respuesta, "buscar");
    }
}
