package ec.edu.monster.servicios;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ec.edu.monster.modelos.data.Universidad;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Consumo de la API pública de universidades de Hipolabs (RQF-011, ECUD11).
 *
 * <p>Es el único lugar del sistema que conoce la dirección de esta API y el formato de su JSON.
 * Hace GET http://universities.hipolabs.com/search?country={pais} con HttpURLConnection,
 * espera como máximo {@value #TIMEOUT_SEGUNDOS} segundos y convierte el arreglo JSON en objetos
 * {@link Universidad} con Gson. Nunca toca la base de datos.</p>
 *
 * <p>Si la API no responde, devuelve un código distinto de 200 o un JSON no válido, lanza
 * IOException «La API de universidades no está disponible, intente más tarde» (502).
 * Si la API responde una lista vacía, devuelve la lista vacía: el controlador decide el 404.</p>
 *
 * <p>La dirección por defecto se puede cambiar con la propiedad del sistema
 * {@code countryapi.universidades.url} (por ejemplo, para probar la API caída).</p>
 *
 * @author Escobar Marcos
 */
public class ConsumoAPIUniversidades {

    static final String NO_DISPONIBLE = "La API de universidades no está disponible, intente más tarde";

    private static final Logger LOG = Logger.getLogger(ConsumoAPIUniversidades.class.getName());

    private static final int TIMEOUT_SEGUNDOS = 15;
    private static final int MAXIMO_REDIRECCIONES = 3;

    private final String URL_BASE;
    private final Gson gson = new Gson();

    public ConsumoAPIUniversidades() {
        this(System.getProperty("countryapi.universidades.url", "http://universities.hipolabs.com"));
    }

    /** Permite apuntar a otra dirección (pruebas con un servidor falso). */
    ConsumoAPIUniversidades(String urlBase) {
        this.URL_BASE = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    /** Universidades del país (nombre del país en inglés, como lo espera la API). */
    public List<Universidad> obtenerPorPais(String pais) throws IOException {
        String url = URL_BASE + "/search?country=" + URLEncoder.encode(pais.trim(), StandardCharsets.UTF_8);
        return convertirRespuesta(getJson(url), pais.trim());
    }

    /**
     * Convierte el arreglo JSON en universidades y descarta las repetidas con el mismo nombre
     * (ECUD11, paso 6).
     *
     * @param paisPedido país de la consulta, por si algún elemento no trae «country»
     */
    List<Universidad> convertirRespuesta(String json, String paisPedido) throws IOException {
        try {
            JsonElement raiz = gson.fromJson(json, JsonElement.class);
            if (raiz == null || !raiz.isJsonArray()) {
                throw new IOException(NO_DISPONIBLE);
            }
            List<Universidad> universidades = new ArrayList<>();
            Set<String> nombres = new HashSet<>();
            for (JsonElement elemento : raiz.getAsJsonArray()) {
                if (!elemento.isJsonObject()) {
                    throw new IOException(NO_DISPONIBLE);
                }
                Universidad u = convertir(elemento.getAsJsonObject());
                if (u == null) {
                    continue;
                }
                if (u.getPais() == null) {
                    u.setPais(recortar(paisPedido, 100));
                }
                if (nombres.add(normalizar(u.getNombre()))) {
                    universidades.add(u);
                }
            }
            return universidades;
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException | ClassCastException e) {
            throw new IOException(NO_DISPONIBLE, e);
        }
    }

    /**
     * Mapeo de una universidad del JSON de Hipolabs:
     * <ul>
     *   <li>nombre: name (sin nombre, el elemento se descarta);</li>
     *   <li>dominio: primer elemento de domains; «Sin dominio» si no tiene;</li>
     *   <li>sitio web: primer elemento de web_pages; «Sin sitio web» si no tiene;</li>
     *   <li>país: country.</li>
     * </ul>
     * Los textos se recortan al largo de las columnas de la tabla universidad.
     */
    private Universidad convertir(JsonObject json) {
        String nombre = texto(json.get("name"));
        if (nombre == null) {
            return null;
        }
        String dominio = primerTexto(json.get("domains"));
        String sitioWeb = primerTexto(json.get("web_pages"));
        String pais = texto(json.get("country"));
        return new Universidad(
                recortar(nombre, 200),
                recortar(dominio == null ? "Sin dominio" : dominio, 150),
                recortar(sitioWeb == null ? "Sin sitio web" : sitioWeb, 250),
                pais == null ? null : recortar(pais, 100));
    }

    private static String texto(JsonElement valor) {
        if (valor == null || !valor.isJsonPrimitive()) {
            return null;
        }
        String texto = valor.getAsString().trim();
        return texto.isEmpty() ? null : texto;
    }

    private static String primerTexto(JsonElement lista) {
        if (lista == null || !lista.isJsonArray()) {
            return null;
        }
        for (JsonElement e : lista.getAsJsonArray()) {
            String valor = texto(e);
            if (valor != null) {
                return valor;
            }
        }
        return null;
    }

    private static String recortar(String texto, int largo) {
        return texto.length() <= largo ? texto : texto.substring(0, largo);
    }

    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }

    /**
     * GET que devuelve el cuerpo si la respuesta es 200. Sigue hasta 3 redirecciones a mano,
     * porque HttpURLConnection no sigue una redirección de HTTP a HTTPS.
     */
    private String getJson(String url) throws IOException {
        String actual = url;
        try {
            for (int intento = 0; intento <= MAXIMO_REDIRECCIONES; intento++) {
                HttpURLConnection conexion = (HttpURLConnection) URI.create(actual).toURL().openConnection();
                try {
                    conexion.setRequestMethod("GET");
                    conexion.setConnectTimeout(TIMEOUT_SEGUNDOS * 1000);
                    conexion.setReadTimeout(TIMEOUT_SEGUNDOS * 1000);
                    conexion.setInstanceFollowRedirects(false);
                    conexion.setRequestProperty("Accept", "application/json");
                    conexion.setRequestProperty("User-Agent", "CountryAPI/1.0");
                    int codigo = conexion.getResponseCode();
                    String destino = conexion.getHeaderField("Location");
                    if (codigo >= 300 && codigo < 400 && destino != null) {
                        actual = URI.create(actual).resolve(destino).toString();
                        continue;
                    }
                    if (codigo != 200) {
                        String mensaje = "La API de universidades respondió " + codigo;
                        LOG.warning(mensaje);
                        throw new IOException(NO_DISPONIBLE);
                    }
                    return leer(conexion.getInputStream());
                } finally {
                    conexion.disconnect();
                }
            }
            throw new IOException(NO_DISPONIBLE);
        } catch (IOException | IllegalArgumentException e) {
            if (e instanceof IOException propia && NO_DISPONIBLE.equals(propia.getMessage())) {
                throw propia;
            }
            LOG.warning(() -> "No se pudo consultar la API de universidades: " + e);
            throw new IOException(NO_DISPONIBLE, e);
        }
    }

    private static String leer(InputStream entrada) throws IOException {
        try (InputStream in = entrada; ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            in.transferTo(salida);
            return salida.toString(StandardCharsets.UTF_8);
        }
    }
}
