package ec.edu.monster.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * «Importar países» (ECUD2). GET muestra la pantalla (con ?confirmar, el panel de
 * confirmación); POST importa y redirige mostrando el resumen.
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "ImportarServlet", urlPatterns = "/importar")
public class ImportarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        peticion.setAttribute("confirmar", peticion.getParameter("confirmar") != null);
        Pagina.mostrar(peticion, respuesta, "importar");
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        modulos.ejecutar(peticion, modulos.getApiControlador()::importar);
        if (!Pagina.sesionExpirada(modulos, peticion, respuesta)) {
            Pagina.redirigir(peticion, respuesta, "/importar");
        }
    }
}
