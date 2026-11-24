package ec.edu.monster.config;

import android.content.Context;
import android.content.SharedPreferences;
import ec.edu.monster.BuildConfig;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Dirección del servidor RESTful y tiempo de espera de las peticiones.
 *
 * <p>La dirección inicial sale de gradle.properties (propiedad {@code servidor}, por defecto
 * http://10.0.2.2:8080/WS_COUNTRYAPI_JAVA_RESTFul/api, que es el equipo anfitrión visto desde el
 * emulador). El usuario la puede cambiar en la pantalla de inicio de sesión sin recompilar; la
 * nueva dirección se guarda en las preferencias de la app.</p>
 *
 * @author Escobar Marcos
 */
public final class ConfigServidor {

    /** Dirección con la que se compiló la app. */
    public static final String URL_POR_DEFECTO = quitarBarraFinal(BuildConfig.URL_SERVIDOR);

    /** Tiempo máximo de espera de cada petición al servidor, en milisegundos. */
    public static final int TIMEOUT_MS = 15000;

    private static final String PREFERENCIAS = "countryapi";
    private static final String CLAVE_URL = "servidor.url";

    private ConfigServidor() {
    }

    /** URL base del servidor, sin «/» al final: la guardada por el usuario o la de compilación. */
    public static String leerUrlBase(Context contexto) {
        String guardada = preferencias(contexto).getString(CLAVE_URL, null);
        return guardada == null || guardada.trim().isEmpty() ? URL_POR_DEFECTO : guardada;
    }

    /** Guarda la nueva dirección (ya validada con {@link #esUrlValida(String)}) y la devuelve normalizada. */
    public static String guardarUrlBase(Context contexto, String url) {
        String normalizada = quitarBarraFinal(url.trim());
        preferencias(contexto).edit().putString(CLAVE_URL, normalizada).apply();
        return normalizada;
    }

    /** True si el texto es una dirección http o https con equipo, por ejemplo http://192.168.1.20:8080/... */
    public static boolean esUrlValida(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(url.trim());
            String esquema = uri.getScheme();
            return ("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema))
                    && uri.getHost() != null && !uri.getHost().isEmpty();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String quitarBarraFinal(String url) {
        String resultado = url;
        while (resultado.endsWith("/")) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado;
    }

    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }
}
