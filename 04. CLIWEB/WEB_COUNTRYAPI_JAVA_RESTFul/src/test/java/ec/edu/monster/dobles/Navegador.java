package ec.edu.monster.dobles;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ec.edu.monster.servicios.ClienteRest;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Navegador simulado para probar servlets y filtros sin Payara: conserva la sesión HTTP entre
 * peticiones (como la cookie de un navegador) y anota a dónde se redirigió, qué JSP se mostró
 * y qué error HTTP se envió.
 */
public final class Navegador {

    public static final String CONTEXTO = "/web";

    private final ServletContext contexto = mock(ServletContext.class);
    private final Map<String, Object> atributosContexto = new HashMap<>();
    private Map<String, Object> sesion;
    private boolean sesionInvalidada;

    /** Resultado de una petición. */
    public static final class Resultado {
        public HttpServletRequest peticion;
        public HttpServletResponse respuesta;
        public final Map<String, Object> atributos = new HashMap<>();
        public String jsp;
        public String redireccion;
        public Integer error;
        public boolean idCambiado;
        public final StringWriter cuerpo = new StringWriter();

        public Object atributo(String nombre) {
            return atributos.get(nombre);
        }
    }

    public Navegador(ClienteRest cliente) {
        atributosContexto.put("clienteRest", cliente);
        when(contexto.getAttribute(anyString())).thenAnswer(i -> atributosContexto.get(i.<String>getArgument(0)));
    }

    public ServletContext contexto() {
        return contexto;
    }

    /** Atributo de la sesión HTTP actual, o null. */
    public Object enSesion(String nombre) {
        return sesion == null ? null : sesion.get(nombre);
    }

    public boolean haySesion() {
        return sesion != null;
    }

    public boolean sesionInvalidada() {
        return sesionInvalidada;
    }

    /**
     * Arma la petición «MÉTODO /ruta» con los parámetros dados en pares nombre, valor. Para
     * ejecutarla, el llamador invoca el servlet o filtro con {@code r.peticion} y
     * {@code r.respuesta}.
     */
    public Resultado peticion(String metodo, String ruta, String... parametros) throws Exception {
        Resultado r = new Resultado();
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i + 1 < parametros.length; i += 2) {
            params.put(parametros[i], parametros[i + 1]);
        }
        HttpServletRequest peticion = mock(HttpServletRequest.class);
        when(peticion.getMethod()).thenReturn(metodo);
        when(peticion.getServletPath()).thenReturn(ruta);
        when(peticion.getContextPath()).thenReturn(CONTEXTO);
        when(peticion.getServletContext()).thenReturn(contexto);
        when(peticion.getParameter(anyString())).thenAnswer(i -> params.get(i.<String>getArgument(0)));
        when(peticion.getAttribute(anyString())).thenAnswer(i -> r.atributos.get(i.<String>getArgument(0)));
        doAnswer(i -> r.atributos.put(i.getArgument(0), i.getArgument(1)))
                .when(peticion).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        when(peticion.getSession(false)).thenAnswer(i -> sesion == null ? null : sesionHttp());
        when(peticion.getSession(true)).thenAnswer(i -> {
            if (sesion == null) {
                sesion = new HashMap<>();
            }
            return sesionHttp();
        });
        when(peticion.getSession()).thenAnswer(i -> peticion.getSession(true));
        when(peticion.changeSessionId()).thenAnswer(i -> {
            r.idCambiado = true;
            return "nuevo-id";
        });
        when(peticion.getRequestDispatcher(anyString())).thenAnswer(i -> {
            RequestDispatcher despachador = mock(RequestDispatcher.class);
            String jsp = i.getArgument(0);
            doAnswer(j -> r.jsp = jsp).when(despachador).forward(org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any());
            return despachador;
        });

        HttpServletResponse respuesta = mock(HttpServletResponse.class);
        doAnswer(i -> r.redireccion = i.getArgument(0)).when(respuesta).sendRedirect(anyString());
        doAnswer(i -> r.error = i.getArgument(0)).when(respuesta).sendError(org.mockito.ArgumentMatchers.anyInt());
        when(respuesta.getWriter()).thenReturn(new PrintWriter(r.cuerpo, true));

        r.peticion = peticion;
        r.respuesta = respuesta;
        return r;
    }

    private HttpSession sesionHttp() {
        Map<String, Object> datos = sesion;
        HttpSession http = mock(HttpSession.class);
        when(http.getId()).thenReturn("sesion-prueba");
        when(http.getAttribute(anyString())).thenAnswer(i -> datos.get(i.<String>getArgument(0)));
        doAnswer(i -> datos.put(i.getArgument(0), i.getArgument(1)))
                .when(http).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        doAnswer(i -> datos.remove(i.<String>getArgument(0))).when(http).removeAttribute(anyString());
        doAnswer(i -> {
            sesionInvalidada = true;
            if (sesion == datos) {
                sesion = null;
            }
            return null;
        }).when(http).invalidate();
        return http;
    }
}
