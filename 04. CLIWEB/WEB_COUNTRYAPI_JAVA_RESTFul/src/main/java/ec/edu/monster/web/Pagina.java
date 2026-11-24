package ec.edu.monster.web;

import ec.edu.monster.vista.ImportacionVistaWeb;
import ec.edu.monster.vista.VistaWeb;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Muestra las JSP y redirige entre pantallas.
 *
 * <p>Después de un POST que funcionó se redirige con GET (patrón POST/Redirect/GET), así
 * recargar la página no repite la operación. Los avisos y el resumen de importación pasan esa
 * redirección guardados un momento en la sesión («flash»).</p>
 *
 * @author Escobar Marcos
 */
public final class Pagina {

    static final String CARPETA = "/WEB-INF/vista/";
    private static final String PREFIJO_FLASH = "flash.";
    private static final String[] FLASH = {VistaWeb.MENSAJE, VistaWeb.ERROR, ImportacionVistaWeb.RESUMEN};

    private Pagina() {
    }

    /** Dibuja WEB-INF/vista/{nombre}.jsp con los atributos de la petición y los avisos pendientes. */
    public static void mostrar(HttpServletRequest peticion, HttpServletResponse respuesta, String nombre)
            throws ServletException, IOException {
        HttpSession sesion = peticion.getSession(false);
        if (sesion != null) {
            for (String clave : FLASH) {
                Object valor = sesion.getAttribute(PREFIJO_FLASH + clave);
                if (valor != null) {
                    if (peticion.getAttribute(clave) == null) {
                        peticion.setAttribute(clave, valor);
                    }
                    sesion.removeAttribute(PREFIJO_FLASH + clave);
                }
            }
        }
        peticion.getRequestDispatcher(CARPETA + nombre + ".jsp").forward(peticion, respuesta);
    }

    /** Redirige (GET) a otra ruta de la aplicación llevando los avisos de esta petición. */
    public static void redirigir(HttpServletRequest peticion, HttpServletResponse respuesta, String ruta)
            throws IOException {
        HttpSession sesion = peticion.getSession(false);
        if (sesion != null) {
            for (String clave : FLASH) {
                Object valor = peticion.getAttribute(clave);
                if (valor != null) {
                    sesion.setAttribute(PREFIJO_FLASH + clave, valor);
                }
            }
        }
        respuesta.sendRedirect(peticion.getContextPath() + ruta);
    }

    /**
     * Cierra la sesión HTTP y vuelve al inicio de sesión mostrando el error de esta petición
     * (por ejemplo, «Su sesión expiró o no es válida...»).
     */
    public static void alInicioSesion(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        Object error = peticion.getAttribute(VistaWeb.ERROR);
        HttpSession anterior = peticion.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        if (error != null) {
            peticion.getSession(true).setAttribute(PREFIJO_FLASH + VistaWeb.ERROR, error);
        }
        respuesta.sendRedirect(peticion.getContextPath() + "/login");
    }

    /** Si el controlador cerró la sesión (token rechazado), redirige al inicio de sesión. */
    static boolean sesionExpirada(ModulosSesion modulos, HttpServletRequest peticion, HttpServletResponse respuesta)
            throws IOException {
        if (modulos.getSesion().estaActiva()) {
            return false;
        }
        alInicioSesion(peticion, respuesta);
        return true;
    }
}
