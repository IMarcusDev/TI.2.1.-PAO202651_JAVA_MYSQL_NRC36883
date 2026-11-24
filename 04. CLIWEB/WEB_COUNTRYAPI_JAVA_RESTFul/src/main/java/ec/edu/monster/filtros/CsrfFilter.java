package ec.edu.monster.filtros;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Protección CSRF con token sincronizado (igual que el cliente web de referencia).
 *
 * <ol>
 *   <li>GET: se asegura de que la sesión tenga un token para que los formularios lo incluyan
 *       (partes/csrf.jspf). Solo GET /login puede crear la sesión.</li>
 *   <li>POST: el parámetro {@code _csrf} debe ser igual al token de la sesión; si no, se
 *       rechaza antes del servlet (POST /login vuelve a un formulario nuevo; los demás, 403).</li>
 * </ol>
 *
 * @author Escobar Marcos
 */
@WebFilter(filterName = "CsrfFilter",
        urlPatterns = {"/login", "/logout", "/menu", "/importar", "/consultar", "/paises", "/buscar", "/reportes",
            "/universidades"})
public class CsrfFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    public static final String ATRIBUTO = "csrfToken";
    public static final String PARAMETRO = "_csrf";

    private static final int BYTES_TOKEN = 32;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Base64.Encoder CODIFICADOR = Base64.getUrlEncoder().withoutPadding();
    private static final String RUTA_LOGIN = "/login";

    @Override
    protected void doFilter(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        boolean esLogin = RUTA_LOGIN.equals(peticion.getServletPath());
        if ("POST".equalsIgnoreCase(peticion.getMethod())) {
            if (!esValido(peticion)) {
                if (esLogin) {
                    // Formulario de inicio vencido o falsificado: se muestra uno nuevo.
                    respuesta.sendRedirect(peticion.getContextPath() + RUTA_LOGIN);
                } else {
                    respuesta.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
            }
        } else {
            HttpSession sesion = esLogin ? peticion.getSession(true) : peticion.getSession(false);
            if (sesion != null && tokenActual(sesion) == null) {
                sesion.setAttribute(ATRIBUTO, nuevoToken());
            }
        }
        cadena.doFilter(peticion, respuesta);
    }

    private static boolean esValido(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        if (sesion == null) {
            return false;
        }
        String esperado = tokenActual(sesion);
        String enviado = peticion.getParameter(PARAMETRO);
        if (esperado == null || enviado == null || enviado.isBlank()) {
            return false;
        }
        // Comparación en tiempo constante: no revela cuántos caracteres coinciden.
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8), enviado.getBytes(StandardCharsets.UTF_8));
    }

    private static String tokenActual(HttpSession sesion) {
        Object token = sesion.getAttribute(ATRIBUTO);
        return token instanceof String valor && !valor.isBlank() ? valor : null;
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[BYTES_TOKEN];
        ALEATORIO.nextBytes(bytes);
        return CODIFICADOR.encodeToString(bytes);
    }
}
