package ec.edu.monster.web;

import ec.edu.monster.vista.ConsultaApiVistaWeb;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * «Consultar país en la API» (ECUD3). GET con ?nombre consulta REST Countries a través del
 * servidor; sin nombre muestra la última consulta del usuario (así, después de «Guardar», la
 * página no gasta otra consulta). POST guarda el país de la fila elegida.
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "ConsultarServlet", urlPatterns = "/consultar")
public class ConsultarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        if (peticion.getParameter("nombre") != null) {
            modulos.ejecutar(peticion, modulos.getApiControlador()::consultar);
            if (Pagina.sesionExpirada(modulos, peticion, respuesta)) {
                return;
            }
        }
        ConsultaApiVistaWeb vista = modulos.getConsulta();
        peticion.setAttribute(ConsultaApiVistaWeb.RESULTADOS, vista.getResultados());
        peticion.setAttribute("nombre", vista.getUltimoNombre());
        Pagina.mostrar(peticion, respuesta, "consultar");
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        modulos.ejecutar(peticion, modulos.getApiControlador()::guardar);
        if (!Pagina.sesionExpirada(modulos, peticion, respuesta)) {
            Pagina.redirigir(peticion, respuesta, "/consultar");
        }
    }
}
