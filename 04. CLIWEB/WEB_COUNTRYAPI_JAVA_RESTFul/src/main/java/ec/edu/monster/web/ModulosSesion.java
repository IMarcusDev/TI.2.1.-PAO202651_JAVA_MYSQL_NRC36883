package ec.edu.monster.web;

import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.controlador.UniversidadControlador;
import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.servicios.LoginServicio;
import ec.edu.monster.servicios.PaisServicio;
import ec.edu.monster.servicios.ReporteServicio;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.servicios.UniversidadServicio;
import ec.edu.monster.vista.BusquedaVistaWeb;
import ec.edu.monster.vista.ConsultaApiVistaWeb;
import ec.edu.monster.vista.ImportacionVistaWeb;
import ec.edu.monster.vista.LoginVistaWeb;
import ec.edu.monster.vista.MenuVistaWeb;
import ec.edu.monster.vista.PaisVistaWeb;
import ec.edu.monster.vista.ReporteVistaWeb;
import ec.edu.monster.vista.UniversidadVistaWeb;
import ec.edu.monster.vista.VistaWeb;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lo que cada usuario tiene en su {@link HttpSession}: su {@link SesionServicio} (con el token
 * del servidor), sus vistas y sus controladores, armados igual que App.construir en el cliente
 * de escritorio.
 *
 * <p>Los controladores son los mismos de los otros clientes. Para atender una petición,
 * {@link #ejecutar(HttpServletRequest, Runnable)} enlaza las vistas con esa petición, corre la
 * acción del controlador y las suelta. Las peticiones de un mismo usuario se atienden de una en
 * una.</p>
 *
 * @author Escobar Marcos
 */
public class ModulosSesion {

    /** Nombre del atributo de la HttpSession. */
    public static final String ATRIBUTO = "modulos";

    private static final Logger LOG = Logger.getLogger(ModulosSesion.class.getName());
    /** Evita que dos peticiones simultáneas del mismo usuario creen módulos distintos. */
    private static final Object CREAR = new Object();

    private final SesionServicio sesion = new SesionServicio();

    private final LoginVistaWeb login = new LoginVistaWeb();
    private final MenuVistaWeb menu = new MenuVistaWeb();
    private final ImportacionVistaWeb importacion = new ImportacionVistaWeb();
    private final ConsultaApiVistaWeb consulta = new ConsultaApiVistaWeb();
    private final PaisVistaWeb paises = new PaisVistaWeb();
    private final BusquedaVistaWeb busqueda = new BusquedaVistaWeb();
    private final ReporteVistaWeb reportes = new ReporteVistaWeb();
    private final UniversidadVistaWeb universidades = new UniversidadVistaWeb();
    private final List<VistaWeb> vistas =
            List.of(login, menu, importacion, consulta, paises, busqueda, reportes, universidades);

    private final LoginControlador loginControlador;
    private final ApiControlador apiControlador;
    private final PaisControlador paisControlador;
    private final ReporteControlador reporteControlador;
    private final UniversidadControlador universidadControlador;

    public ModulosSesion(ClienteRest cliente) {
        PaisServicio paisServicio = new PaisServicio(cliente);
        loginControlador = new LoginControlador(login, menu, new LoginServicio(cliente), sesion);
        apiControlador = new ApiControlador(consulta, importacion, paisServicio, sesion);
        paisControlador = new PaisControlador(paises, busqueda, paisServicio, sesion);
        reporteControlador = new ReporteControlador(reportes, new ReporteServicio(cliente), sesion);
        universidadControlador = new UniversidadControlador(universidades, new UniversidadServicio(cliente), sesion);
    }

    /** Los módulos del usuario; si no tiene sesión HTTP o módulos, se crean. */
    public static ModulosSesion de(HttpServletRequest peticion) {
        HttpSession sesionHttp = peticion.getSession(true);
        synchronized (CREAR) {
            ModulosSesion modulos = (ModulosSesion) sesionHttp.getAttribute(ATRIBUTO);
            if (modulos == null) {
                modulos = new ModulosSesion(AplicacionWeb.cliente(peticion.getServletContext()));
                sesionHttp.setAttribute(ATRIBUTO, modulos);
            }
            return modulos;
        }
    }

    /** Los módulos del usuario sin crear nada, o null. */
    public static ModulosSesion existente(HttpServletRequest peticion) {
        HttpSession sesionHttp = peticion.getSession(false);
        return sesionHttp == null ? null : (ModulosSesion) sesionHttp.getAttribute(ATRIBUTO);
    }

    /** True si hay un usuario con sesión abierta (token del servidor). */
    public static boolean conSesion(HttpServletRequest peticion) {
        ModulosSesion modulos = existente(peticion);
        return modulos != null && modulos.sesion.estaActiva();
    }

    /**
     * Ejecuta una acción del controlador con las vistas enlazadas a esta petición. Un error
     * inesperado se muestra como aviso y no rompe la página.
     */
    public synchronized void ejecutar(HttpServletRequest peticion, Runnable accion) {
        vistas.forEach(vista -> vista.atender(peticion));
        try {
            accion.run();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Error inesperado en una acción", e);
            peticion.setAttribute(VistaWeb.ERROR, ClienteRest.RESPUESTA_NO_VALIDA);
        } finally {
            vistas.forEach(VistaWeb::terminar);
        }
    }

    public SesionServicio getSesion() {
        return sesion;
    }

    public LoginControlador getLoginControlador() {
        return loginControlador;
    }

    public ApiControlador getApiControlador() {
        return apiControlador;
    }

    public PaisControlador getPaisControlador() {
        return paisControlador;
    }

    public ReporteControlador getReporteControlador() {
        return reporteControlador;
    }

    public UniversidadControlador getUniversidadControlador() {
        return universidadControlador;
    }

    public PaisVistaWeb getPaises() {
        return paises;
    }

    public BusquedaVistaWeb getBusqueda() {
        return busqueda;
    }

    public ConsultaApiVistaWeb getConsulta() {
        return consulta;
    }
}
