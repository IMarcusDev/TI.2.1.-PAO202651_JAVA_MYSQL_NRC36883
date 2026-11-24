package ec.edu.monster.web;

import ec.edu.monster.modelos.Pais;
import ec.edu.monster.vista.PaisVistaWeb;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Gestión de países (ECUD4).
 *
 * <ul>
 *   <li>GET: lista los países (o los que coinciden con ?q=). Además, ?nuevo abre el formulario
 *       vacío, ?editar=ID lo abre con los datos del país y ?eliminar=ID muestra la
 *       confirmación.</li>
 *   <li>POST accion=registrar|actualizar|eliminar: llama al controlador. Si funcionó, redirige
 *       a la lista con el mensaje; si los datos no son válidos o el país ya existe, vuelve a
 *       mostrar el formulario con lo escrito y el error.</li>
 * </ul>
 *
 * @author Escobar Marcos
 */
@WebServlet(name = "PaisesServlet", urlPatterns = "/paises")
public class PaisesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        // Regiones de la lista desplegable del formulario.
        peticion.setAttribute("regiones", Pais.REGIONES);
        super.service(peticion, respuesta);
    }

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        boolean busqueda = peticion.getParameter("q") != null;
        modulos.ejecutar(peticion, busqueda ? modulos.getPaisControlador()::buscar : modulos.getPaisControlador()::listar);
        if (Pagina.sesionExpirada(modulos, peticion, respuesta)) {
            return;
        }
        PaisVistaWeb vista = modulos.getPaises();
        if (peticion.getParameter("nuevo") != null) {
            // Población -1 y área NaN: el formulario los muestra vacíos.
            peticion.setAttribute(PaisVistaWeb.FORMULARIO, new Pais(null, null, null, null, -1, Double.NaN, null));
        } else if (peticion.getParameter("editar") != null) {
            Pais pais = vista.buscar(peticion.getParameter("editar"));
            if (pais != null) {
                peticion.setAttribute(PaisVistaWeb.FORMULARIO, pais.copia());
            }
        } else if (peticion.getParameter("eliminar") != null) {
            peticion.setAttribute(PaisVistaWeb.ELIMINAR, vista.buscar(peticion.getParameter("eliminar")));
        }
        Pagina.mostrar(peticion, respuesta, "paises");
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ModulosSesion modulos = ModulosSesion.de(peticion);
        String accion = peticion.getParameter("accion");
        Runnable operacion = switch (accion == null ? "" : accion) {
            case "registrar" -> modulos.getPaisControlador()::registrar;
            case "actualizar" -> modulos.getPaisControlador()::actualizar;
            case "eliminar" -> modulos.getPaisControlador()::eliminar;
            default -> null;
        };
        if (operacion != null) {
            modulos.ejecutar(peticion, operacion);
            if (Pagina.sesionExpirada(modulos, peticion, respuesta)) {
                return;
            }
        }
        if (peticion.getAttribute(PaisVistaWeb.FORMULARIO) != null) {
            // Datos rechazados: el formulario se vuelve a mostrar sobre la última tabla.
            peticion.setAttribute(PaisVistaWeb.PAISES, modulos.getPaises().getUltimaTabla());
            Pagina.mostrar(peticion, respuesta, "paises");
            return;
        }
        Pagina.redirigir(peticion, respuesta, "/paises");
    }
}
