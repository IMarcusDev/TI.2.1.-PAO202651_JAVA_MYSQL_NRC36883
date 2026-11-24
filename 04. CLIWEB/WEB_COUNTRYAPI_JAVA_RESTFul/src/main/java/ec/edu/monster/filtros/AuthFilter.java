package ec.edu.monster.filtros;

import ec.edu.monster.web.ModulosSesion;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Control de acceso de todas las pantallas protegidas, antes de llegar al servlet: sin una
 * sesión con token del servidor se redirige al inicio de sesión.
 *
 * @author Escobar Marcos
 */
@WebFilter(filterName = "AuthFilter",
        urlPatterns = {"/menu", "/importar", "/consultar", "/paises", "/buscar", "/reportes", "/universidades"})
public class AuthFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        if (!ModulosSesion.conSesion(peticion)) {
            respuesta.sendRedirect(peticion.getContextPath() + "/login");
            return;
        }
        cadena.doFilter(peticion, respuesta);
    }
}
