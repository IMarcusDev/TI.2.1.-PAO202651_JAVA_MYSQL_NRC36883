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
import ec.edu.monster.vista.BusquedaPanel;
import ec.edu.monster.vista.ConsultaApiPanel;
import ec.edu.monster.vista.ImportacionPanel;
import ec.edu.monster.vista.LoginPanel;
import ec.edu.monster.vista.PaisPanel;
import ec.edu.monster.vista.ReportePanel;
import ec.edu.monster.vista.UniversidadPanel;
import ec.edu.monster.vista.VentanaPrincipal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del cliente de escritorio. Crea el modelo (servicios y sesión), las vistas
 * Swing y los controladores, y los conecta (vista → controlador → servicios → ClienteRest).
 *
 * @author Escobar Marcos
 */
public final class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());

    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
                LOG.log(Level.FINE, "Se usa el look and feel por defecto", e);
            }
            construir(new ClienteRest()).setVisible(true);
        });
    }

    /** Arma la ventana completa contra el servidor del cliente indicado. */
    public static VentanaPrincipal construir(ClienteRest cliente) {
        SesionServicio sesion = new SesionServicio();
        VentanaPrincipal ventana = new VentanaPrincipal(cliente.getUrlBase(), cliente::servidorDisponible);

        // Inicio y cierre de sesión
        LoginPanel login = new LoginPanel(ventana);
        ventana.setLogin(login);
        LoginControlador loginControlador = new LoginControlador(login, ventana, new LoginServicio(cliente), sesion);
        login.setControlador(loginControlador);

        // Países: CRUD, búsqueda, importación y consulta en la API
        PaisServicio paisServicio = new PaisServicio(cliente);
        ImportacionPanel importacion = new ImportacionPanel(ventana);
        ConsultaApiPanel consulta = new ConsultaApiPanel(ventana);
        ApiControlador apiControlador = new ApiControlador(consulta, importacion, paisServicio, sesion);
        importacion.setControlador(apiControlador);
        consulta.setControlador(apiControlador);

        PaisPanel paises = new PaisPanel(ventana);
        BusquedaPanel busqueda = new BusquedaPanel(ventana);
        PaisControlador paisControlador = new PaisControlador(paises, busqueda, paisServicio, sesion);
        paises.setControlador(paisControlador);
        busqueda.setControlador(paisControlador);

        // Reportes y universidades
        ReportePanel reportes = new ReportePanel(ventana);
        ReporteControlador reporteControlador = new ReporteControlador(reportes, new ReporteServicio(cliente), sesion);
        reportes.setControlador(reporteControlador);

        UniversidadPanel universidades = new UniversidadPanel(ventana);
        UniversidadControlador universidadControlador =
                new UniversidadControlador(universidades, new UniversidadServicio(cliente), sesion);
        universidades.setControlador(universidadControlador);

        // Opciones del menú, en el orden del ECUD1
        ventana.agregarPantalla("importar", "Importar países",
                "Trae todos los países desde la API REST Countries.", importacion, null);
        ventana.agregarPantalla("consultar", "Consultar país en la API",
                "Busca un país por nombre en la API y guárdelo.", consulta, null);
        ventana.agregarPantalla("paises", "Países",
                "Lista, registra, edita y elimina los países guardados.", paises, paisControlador::listar);
        ventana.agregarPantalla("buscar", "Buscar y filtrar",
                "Busca por nombre, capital y región con filtros dinámicos.", busqueda, paisControlador::buscarYFiltrar);
        ventana.agregarPantalla("reportes", "Reportes",
                "Países por región, país más poblado y promedio poblacional.", reportes, reporteControlador::generar);
        ventana.agregarPantalla("universidades", "Universidades",
                "Importa y consulta las universidades de un país.", universidades, universidadControlador::listar);
        ventana.setAccionCerrarSesion(loginControlador::cerrarSesion);
        return ventana;
    }
}
