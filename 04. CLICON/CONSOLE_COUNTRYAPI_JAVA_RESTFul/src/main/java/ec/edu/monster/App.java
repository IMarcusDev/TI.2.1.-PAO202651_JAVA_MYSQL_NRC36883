package ec.edu.monster;

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
import ec.edu.monster.vista.BusquedaConsola;
import ec.edu.monster.vista.Consola;
import ec.edu.monster.vista.ConsultaApiConsola;
import ec.edu.monster.vista.ImportacionConsola;
import ec.edu.monster.vista.LoginConsola;
import ec.edu.monster.vista.MenuConsola;
import ec.edu.monster.vista.PaisConsola;
import ec.edu.monster.vista.PantallaConsola;
import ec.edu.monster.vista.ReporteConsola;
import ec.edu.monster.vista.UniversidadConsola;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Punto de entrada del cliente de consola. Crea el modelo (servicios y sesión), las pantallas
 * de texto y los controladores, los conecta (vista → controlador → servicios → ClienteRest) y
 * recorre el ciclo inicio de sesión → menú principal hasta que el usuario sale.
 *
 * @author Escobar Marcos
 */
public final class App {

    private final Consola consola;
    private final SesionServicio sesion = new SesionServicio();
    private final LoginConsola login;
    private final MenuConsola menu;
    private final LoginControlador loginControlador;
    private final ImportacionConsola importacion;
    private final ConsultaApiConsola consulta;
    private final PaisConsola paises;
    private final BusquedaConsola busqueda;
    private final ReporteConsola reportes;
    private final UniversidadConsola universidades;

    public App(Consola consola, ClienteRest cliente) {
        this.consola = consola;

        // Inicio y cierre de sesión
        login = new LoginConsola(consola, sesion);
        menu = new MenuConsola(consola, sesion, cliente.getUrlBase(), cliente::servidorDisponible);
        loginControlador = new LoginControlador(login, menu, new LoginServicio(cliente), sesion);

        // Países: CRUD, búsqueda, importación y consulta en la API
        PaisServicio paisServicio = new PaisServicio(cliente);
        importacion = new ImportacionConsola(consola, sesion);
        consulta = new ConsultaApiConsola(consola, sesion);
        ApiControlador apiControlador = new ApiControlador(consulta, importacion, paisServicio, sesion);
        importacion.setControlador(apiControlador);
        consulta.setControlador(apiControlador);

        paises = new PaisConsola(consola, sesion);
        busqueda = new BusquedaConsola(consola, sesion);
        PaisControlador paisControlador = new PaisControlador(paises, busqueda, paisServicio, sesion);
        paises.setControlador(paisControlador);
        busqueda.setControlador(paisControlador);

        // Reportes y universidades
        reportes = new ReporteConsola(consola, sesion);
        reportes.setControlador(new ReporteControlador(reportes, new ReporteServicio(cliente), sesion));
        universidades = new UniversidadConsola(consola, sesion);
        universidades.setControlador(
                new UniversidadControlador(universidades, new UniversidadServicio(cliente), sesion));
    }

    public static void main(String[] args) {
        // Los avisos internos (por ejemplo, de Gson) no deben mezclarse con las pantallas.
        Logger.getLogger("").setLevel(Level.SEVERE);
        new App(Consola.delSistema(), new ClienteRest()).ejecutar();
    }

    /** Inicio de sesión y menú hasta que el usuario escribe 0 como usuario o se acaba la entrada. */
    public void ejecutar() {
        try {
            boolean mostrarFormulario = true;
            while (true) {
                if (mostrarFormulario) {
                    login.mostrarFormulario();
                }
                login.tomarFormularioMostrado();
                PantallaConsola.ejecutar(consola, loginControlador::iniciarSesion);
                // Si el inicio falla, se vuelve a pedir usuario y clave sin repetir el encabezado.
                mostrarFormulario = false;
                if (sesion.estaActiva()) {
                    while (sesion.estaActiva()) {
                        atender(menu.leerOpcion());
                    }
                    // Al cerrar sesión el controlador ya mostró el formulario; si la sesión
                    // expiró, todavía no.
                    mostrarFormulario = !login.tomarFormularioMostrado();
                }
            }
        } catch (Consola.Salida e) {
            consola.lineaEnBlanco();
            consola.texto("Hasta luego.");
        }
    }

    private void atender(int opcion) {
        switch (opcion) {
            case MenuConsola.IMPORTAR -> importacion.mostrar();
            case MenuConsola.CONSULTAR -> consulta.mostrar();
            case MenuConsola.PAISES -> paises.mostrar();
            case MenuConsola.BUSCAR -> busqueda.mostrar();
            case MenuConsola.REPORTES -> reportes.mostrar();
            case MenuConsola.UNIVERSIDADES -> universidades.mostrar();
            case MenuConsola.CERRAR_SESION -> PantallaConsola.ejecutar(consola, loginControlador::cerrarSesion);
            default -> {
                // MenuConsola ya mostró «Opción no válida».
            }
        }
    }
}
