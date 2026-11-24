package ec.edu.monster.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Reportes (ECUD6): se generan en cada GET.
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "ReportesServlet", urlPatterns = "/reportes")
public class ReportesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        modulos.ejecutar(peticion, modulos.getReporteControlador()::generar);
        if (!Pagina.sesionExpirada(modulos, peticion, respuesta)) {
            Pagina.mostrar(peticion, respuesta, "reportes");
        }
    }
}
