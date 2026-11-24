package ec.edu.monster;

import android.app.Application;
import android.content.Context;
import ec.edu.monster.config.ConfigServidor;
import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.servicios.LoginServicio;
import ec.edu.monster.servicios.PaisServicio;
import ec.edu.monster.servicios.ReporteServicio;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.servicios.UniversidadServicio;

/**
 * Crea una sola vez el modelo del cliente: el {@link ClienteRest}, la sesión y los servicios de
 * cada módulo. La actividad principal toma estos objetos para armar los controladores.
 *
 * @author Escobar Marcos
 */
public class AplicacionCountryApi extends Application {

    private ClienteRest cliente;
    private SesionServicio sesion;
    private LoginServicio loginServicio;
    private PaisServicio paisServicio;
    private ReporteServicio reporteServicio;
    private UniversidadServicio universidadServicio;

    public static AplicacionCountryApi de(Context contexto) {
        return (AplicacionCountryApi) contexto.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cliente = new ClienteRest(ConfigServidor.leerUrlBase(this));
        sesion = new SesionServicio();
        loginServicio = new LoginServicio(cliente);
        paisServicio = new PaisServicio(cliente);
        reporteServicio = new ReporteServicio(cliente);
        universidadServicio = new UniversidadServicio(cliente);
    }

    /** Guarda la nueva dirección del servidor y apunta el cliente a ella. */
    public String cambiarServidor(String url) {
        String normalizada = ConfigServidor.guardarUrlBase(this, url);
        cliente.setUrlBase(normalizada);
        return normalizada;
    }

    public ClienteRest getCliente() {
        return cliente;
    }

    public SesionServicio getSesion() {
        return sesion;
    }

    public LoginServicio getLoginServicio() {
        return loginServicio;
    }

    public PaisServicio getPaisServicio() {
        return paisServicio;
    }

    public ReporteServicio getReporteServicio() {
        return reporteServicio;
    }

    public UniversidadServicio getUniversidadServicio() {
        return universidadServicio;
    }
}
