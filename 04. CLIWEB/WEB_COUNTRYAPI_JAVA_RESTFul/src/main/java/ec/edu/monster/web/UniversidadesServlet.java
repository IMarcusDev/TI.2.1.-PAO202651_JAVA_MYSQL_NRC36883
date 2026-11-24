package ec.edu.monster.web;

import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.vista.UniversidadVistaWeb;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Universidades (ECUD10). GET muestra las guardadas del país (?pais=, por defecto Ecuador);
 * POST con accion=importar importa las de ese país; con accion=listar solo cambia de país.
 * En los dos casos redirige a la lista.
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "UniversidadesServlet", urlPatterns = "/universidades")
public class UniversidadesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        modulos.ejecutar(peticion, modulos.getUniversidadControlador()::listar);
        if (!Pagina.sesionExpirada(modulos, peticion, respuesta)) {
            Pagina.mostrar(peticion, respuesta, "universidades");
        }
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        if ("listar".equals(peticion.getParameter("accion"))) {
            // «Ver guardadas»: solo cambia de país (la lista la carga el GET).
            String pais = peticion.getParameter(UniversidadVistaWeb.PAIS);
            Pagina.redirigir(peticion, respuesta,
                    "/universidades?pais=" + ClienteRest.codificar(pais == null ? "" : pais.trim()));
            return;
        }
        ModulosSesion modulos = ModulosSesion.de(peticion);
        modulos.ejecutar(peticion, modulos.getUniversidadControlador()::importar);
        if (Pagina.sesionExpirada(modulos, peticion, respuesta)) {
            return;
        }
        Object pais = peticion.getAttribute(UniversidadVistaWeb.PAIS);
        Pagina.redirigir(peticion, respuesta,
                "/universidades?pais=" + ClienteRest.codificar(pais == null ? "" : pais.toString()));
    }
}
