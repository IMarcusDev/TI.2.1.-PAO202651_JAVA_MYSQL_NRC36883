package ec.edu.monster.config;

import ec.edu.monster.ws.LoginWebService;
import ec.edu.monster.ws.PaisWebService;
import ec.edu.monster.ws.ReporteWebService;
import ec.edu.monster.ws.UniversidadWebService;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

/**
 * Publica los servicios RESTful bajo la ruta base /api
 * (http://&lt;servidor&gt;:8080/WS_COUNTRYAPI_JAVA_RESTFul/api).
 *
 * @author Escobar Marcos
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                LoginWebService.class,
                PaisWebService.class,
                ReporteWebService.class,
                UniversidadWebService.class);
    }
}
