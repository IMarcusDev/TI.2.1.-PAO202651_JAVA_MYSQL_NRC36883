package ec.edu.monster.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dirección del servidor RESTful y tiempo de espera de las peticiones.
 *
 * <p>La URL se lee de servidor.properties (clave {@code servidor.url}) para poder cambiar la IP
 * sin recompilar. Se busca, en este orden:</p>
 * <ol>
 *   <li>el archivo indicado con {@code -Dcountryapi.config=<ruta>};</li>
 *   <li>servidor.properties en la carpeta de trabajo (Maven lo copia junto al jar);</li>
 *   <li>servidor.properties dentro del jar.</li>
 * </ol>
 * Si no se encuentra, se usa http://localhost:8080/WS_COUNTRYAPI_JAVA_RESTFul/api.
 *
 * @author Escobar Marcos
 */
public final class ConfigServidor {

    public static final String ARCHIVO = "servidor.properties";
    public static final String URL_POR_DEFECTO = "http://localhost:8080/WS_COUNTRYAPI_JAVA_RESTFul/api";

    /** Tiempo máximo de espera de cada petición al servidor, en milisegundos. */
    public static final int TIMEOUT_MS = 15000;

    /** URL base del servidor, sin «/» al final. */
    public static final String URL_BASE;

    private static final Logger LOG = Logger.getLogger(ConfigServidor.class.getName());

    static {
        URL_BASE = quitarBarraFinal(cargar().getProperty("servidor.url", URL_POR_DEFECTO).trim());
    }

    private ConfigServidor() {
    }

    private static Properties cargar() {
        Properties propiedades = new Properties();
        String ruta = System.getProperty("countryapi.config");
        Path archivo = ruta != null && !ruta.isBlank() ? Path.of(ruta.trim()) : Path.of(ARCHIVO);
        try {
            if (Files.isReadable(archivo)) {
                try (InputStream entrada = Files.newInputStream(archivo)) {
                    propiedades.load(entrada);
                    return propiedades;
                }
            }
            try (InputStream entrada = ConfigServidor.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
                if (entrada != null) {
                    propiedades.load(entrada);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo leer " + ARCHIVO + "; se usa la dirección por defecto", e);
        }
        return propiedades;
    }

    private static String quitarBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
