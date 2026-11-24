package ec.edu.monster.servicios;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.data.Region;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Consumo de la API pública REST Countries (RQF-008, ECUD8).
 *
 * <p>Es el único lugar del sistema que conoce la dirección de la API y el formato de su JSON.
 * Hace peticiones GET con HttpURLConnection, espera cada respuesta como máximo
 * {@value #TIMEOUT_SEGUNDOS} segundos y convierte cada país del JSON en un objeto {@link Pais}
 * con Gson. Nunca toca la base de datos.</p>
 *
 * <p><b>Versión v5.</b> La v3.1 que describe la documentación fue retirada por el proveedor
 * (ver DUDAS.md, C1). La v5 cambia lo siguiente:</p>
 * <ul>
 *   <li>dirección https://api.restcountries.com/countries/v5;</li>
 *   <li>clave obligatoria en el encabezado «Authorization: Bearer &lt;clave&gt;»;</li>
 *   <li>respuesta {"data": {"objects": [...], "meta": {"more": ...}}}, paginada de 100 en 100;</li>
 *   <li>nombres de campos nuevos (ver {@link #convertir(JsonObject)}).</li>
 * </ul>
 *
 * <p>La clave se busca, en este orden, en la propiedad del sistema
 * {@code countryapi.restcountries.clave}, en la variable de entorno
 * {@code COUNTRYAPI_RESTCOUNTRIES_CLAVE} y en el archivo api.properties del classpath
 * ({@code restcountries.clave}). Nunca está escrita en el código.</p>
 *
 * <p>Errores que informa:</p>
 * <ul>
 *   <li>sin clave, sin conexión, tiempo agotado o código distinto de 200 → IOException
 *       «La API REST Countries no está disponible, intente más tarde» (502);</li>
 *   <li>JSON que no tiene el formato esperado → IOException
 *       «La API REST Countries devolvió una respuesta no válida» (502);</li>
 *   <li>búsqueda por nombre sin coincidencias → NoSuchElementException
 *       «No se encontraron países con ese nombre en la API» (404).</li>
 * </ul>
 *
 * <p>Para probar la API caída, la dirección se puede cambiar con la propiedad del sistema
 * {@code countryapi.restcountries.url}.</p>
 *
 * @author Escobar Marcos
 */
public class ConsumoAPI {

    static final String NO_DISPONIBLE = "La API REST Countries no está disponible, intente más tarde";
    static final String RESPUESTA_NO_VALIDA = "La API REST Countries devolvió una respuesta no válida";
    static final String SIN_COINCIDENCIAS = "No se encontraron países con ese nombre en la API";

    private static final Logger LOG = Logger.getLogger(ConsumoAPI.class.getName());

    /** Solo los campos que usa el sistema, para que la respuesta sea pequeña y rápida. */
    private static final String CAMPOS =
            "names.common,names.translations.spa,capitals,region,subregion,population,area,currencies";
    private static final int TIMEOUT_SEGUNDOS = 15;
    /** Máximo de países por página en el plan gratuito. */
    private static final int POR_PAGINA = 100;
    /** Tope de páginas por importación (hoy son unos 250 países: 3 páginas). */
    private static final int MAXIMO_PAGINAS = 10;
    /** Largo máximo de las columnas VARCHAR(100) de la tabla pais. */
    private static final int LARGO_MAXIMO = 100;

    /** Regiones de la API (en inglés) y su nombre en el catálogo region. */
    private static final Map<String, String> REGIONES = Map.of(
            "Americas", "América",
            "Europe", "Europa",
            "Asia", "Asia",
            "Africa", "África",
            "Oceania", "Oceanía",
            "Antarctic", "Antártida",
            "Antarctica", "Antártida");

    private final String URL_BASE;
    private final String clave;
    private final Gson gson = new Gson();

    public ConsumoAPI() {
        this(System.getProperty("countryapi.restcountries.url", "https://api.restcountries.com/countries/v5"),
                leerClave());
    }

    /** Permite apuntar a otra dirección y usar otra clave (pruebas con un servidor falso). */
    ConsumoAPI(String urlBase, String clave) {
        this.URL_BASE = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.clave = clave;
    }

    /**
     * Todos los países (Importar Países). La API entrega como máximo 100 por página, así que
     * se piden páginas con offset hasta que meta.more sea false.
     */
    public List<Pais> obtenerTodos() throws IOException {
        List<Pais> paises = new ArrayList<>();
        int offset = 0;
        for (int pagina = 0; pagina < MAXIMO_PAGINAS; pagina++) {
            RespuestaHttp respuesta = get(URL_BASE + "?limit=" + POR_PAGINA + "&offset=" + offset
                    + "&response_fields=" + CAMPOS);
            if (respuesta.codigo() != 200) {
                LOG.warning(() -> "REST Countries (listado) respondió " + respuesta.codigo());
                throw new IOException(NO_DISPONIBLE);
            }
            Pagina actual = leerPagina(respuesta.cuerpo());
            paises.addAll(actual.paises());
            if (!actual.hayMas() || actual.recibidos() == 0) {
                return paises;
            }
            offset += actual.recibidos();
        }
        LOG.warning("REST Countries: se alcanzó el tope de páginas; la importación puede estar incompleta");
        return paises;
    }

    /**
     * Países cuyo nombre (común, oficial, alternativo o nativo) contiene el texto
     * (Consultar País en la API). Usa el agregado «name» de la v5.
     */
    public List<Pais> buscarPorNombre(String nombre) throws IOException {
        String q = URLEncoder.encode(nombre.trim(), StandardCharsets.UTF_8);
        RespuestaHttp respuesta = get(URL_BASE + "/name?q=" + q + "&limit=" + POR_PAGINA
                + "&response_fields=" + CAMPOS);
        if (respuesta.codigo() == 404) {
            throw new NoSuchElementException(SIN_COINCIDENCIAS);
        }
        if (respuesta.codigo() != 200) {
            LOG.warning(() -> "REST Countries (búsqueda) respondió " + respuesta.codigo());
            throw new IOException(NO_DISPONIBLE);
        }
        List<Pais> paises = leerPagina(respuesta.cuerpo()).paises();
        if (paises.isEmpty()) {
            throw new NoSuchElementException(SIN_COINCIDENCIAS);
        }
        return paises;
    }

    /** Convierte el cuerpo de una respuesta de la v5 en países (se usa en las pruebas). */
    List<Pais> convertirRespuesta(String json) throws IOException {
        return leerPagina(json).paises();
    }

    /**
     * Lee {"data": {"objects": [...], "meta": {"more": true|false}}}. Los países cuya región no
     * está en el catálogo se descartan y se registran en el log (ECUD8, flujo alternativo 4).
     */
    private Pagina leerPagina(String json) throws IOException {
        try {
            JsonElement raiz = gson.fromJson(json, JsonElement.class);
            JsonElement data = raiz != null && raiz.isJsonObject() ? raiz.getAsJsonObject().get("data") : null;
            JsonElement objetos = data != null && data.isJsonObject() ? data.getAsJsonObject().get("objects") : null;
            if (objetos == null || !objetos.isJsonArray()) {
                throw new IOException(RESPUESTA_NO_VALIDA);
            }
            JsonArray arreglo = objetos.getAsJsonArray();
            List<Pais> paises = new ArrayList<>(arreglo.size());
            for (JsonElement elemento : arreglo) {
                if (!elemento.isJsonObject()) {
                    throw new IOException(RESPUESTA_NO_VALIDA);
                }
                Pais pais = convertir(elemento.getAsJsonObject());
                if (pais != null) {
                    paises.add(pais);
                }
            }
            JsonElement meta = data.getAsJsonObject().get("meta");
            JsonElement mas = meta != null && meta.isJsonObject() ? meta.getAsJsonObject().get("more") : null;
            boolean hayMas = mas != null && mas.isJsonPrimitive() && mas.getAsBoolean();
            return new Pagina(paises, arreglo.size(), hayMas);
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException
                | NumberFormatException | ClassCastException e) {
            // El cuerpo no es JSON o algún campo no tiene el tipo esperado.
            throw new IOException(RESPUESTA_NO_VALIDA, e);
        }
    }

    /**
     * Mapeo de un país del JSON de REST Countries v5 a los siete datos del taller
     * (ERS, tabla 41, con los nombres de campo de la v5):
     * <ul>
     *   <li>nombre: names.translations.spa.common; si no existe, names.common;</li>
     *   <li>capital: capitals[].name, la marcada como primary o la primera; «Sin capital» si no tiene;</li>
     *   <li>región: region traducida al español (Americas → América, ...);</li>
     *   <li>subregión: subregion tal como viene; «Sin subregión» si no tiene;</li>
     *   <li>población: population como entero largo;</li>
     *   <li>área: area.kilometers en km² (0 si no viene);</li>
     *   <li>moneda: «name (code)» de la primera de currencies; «Sin moneda» si no tiene.</li>
     * </ul>
     *
     * @return el país, o null si su región no está en el catálogo
     */
    private Pais convertir(JsonObject json) throws IOException {
        String nombre = texto(json, "names", "translations", "spa", "common");
        if (nombre == null) {
            nombre = texto(json, "names", "common");
        }
        if (nombre == null) {
            throw new IOException(RESPUESTA_NO_VALIDA);
        }

        String regionApi = texto(json, "region");
        String region = traducirRegion(regionApi);
        if (region == null) {
            String descartado = nombre;
            LOG.warning(() -> "País descartado por región desconocida: " + descartado + " (" + regionApi + ")");
            return null;
        }

        String capital = capitalPrincipal(json.get("capitals"));
        String subregion = texto(json, "subregion");

        // Población y área no pueden ser negativas (CHECK de la tabla pais).
        long poblacion = Math.max(0L, numero(json.get("population")).longValue());
        JsonElement area = json.get("area");
        double km2 = area != null && area.isJsonObject()
                ? Math.max(0.0, numero(area.getAsJsonObject().get("kilometers")).doubleValue())
                : 0.0;

        return new Pais(
                recortar(nombre),
                recortar(capital == null ? "Sin capital" : capital),
                new Region(0, region),
                recortar(subregion == null ? "Sin subregión" : subregion),
                poblacion,
                km2,
                recortar(moneda(json.get("currencies"))));
    }

    /** Traduce la región de la API al español del catálogo; null si no la reconoce. */
    private String traducirRegion(String region) {
        return region == null ? null : REGIONES.get(region.trim());
    }

    /**
     * La v5 entrega capitals como [{"name": "Quito", "attributes": {"primary": true, ...}}].
     * Se toma la capital marcada como primary; si ninguna lo está, la primera con nombre.
     */
    private static String capitalPrincipal(JsonElement capitales) {
        if (capitales == null || !capitales.isJsonArray()) {
            return null;
        }
        String primera = null;
        for (JsonElement e : capitales.getAsJsonArray()) {
            if (!e.isJsonObject()) {
                continue;
            }
            String nombre = texto(e.getAsJsonObject(), "name");
            if (nombre == null) {
                continue;
            }
            JsonElement principal = e.getAsJsonObject().has("attributes")
                    && e.getAsJsonObject().get("attributes").isJsonObject()
                    ? e.getAsJsonObject().getAsJsonObject("attributes").get("primary") : null;
            if (principal != null && principal.isJsonPrimitive() && principal.getAsBoolean()) {
                return nombre;
            }
            if (primera == null) {
                primera = nombre;
            }
        }
        return primera;
    }

    /**
     * La v5 entrega currencies como [{"code": "USD", "name": "United States dollar", "symbol": "$"}].
     * Se toma la primera moneda: «United States dollar (USD)».
     */
    private static String moneda(JsonElement monedas) {
        if (monedas == null || !monedas.isJsonArray()) {
            return "Sin moneda";
        }
        for (JsonElement e : monedas.getAsJsonArray()) {
            if (!e.isJsonObject()) {
                continue;
            }
            String codigo = texto(e.getAsJsonObject(), "code");
            String nombre = texto(e.getAsJsonObject(), "name");
            if (nombre != null && codigo != null) {
                return nombre + " (" + codigo + ")";
            }
            if (nombre != null || codigo != null) {
                return nombre != null ? nombre : codigo;
            }
        }
        return "Sin moneda";
    }

    /** Texto en la ruta indicada (por ejemplo names → common), o null si no existe o está vacío. */
    private static String texto(JsonObject json, String... ruta) {
        JsonElement actual = json;
        for (String paso : ruta) {
            if (actual == null || !actual.isJsonObject()) {
                return null;
            }
            actual = actual.getAsJsonObject().get(paso);
        }
        if (actual == null || !actual.isJsonPrimitive()) {
            return null;
        }
        String valor = actual.getAsString().trim();
        return valor.isEmpty() ? null : valor;
    }

    /** Número del campo, o 0 si no viene. */
    private static Number numero(JsonElement valor) {
        return valor == null || valor.isJsonNull() ? 0 : valor.getAsNumber();
    }

    private static String recortar(String texto) {
        return texto.length() <= LARGO_MAXIMO ? texto : texto.substring(0, LARGO_MAXIMO);
    }

    /**
     * GET con la clave y tiempo de espera; cualquier falla de red se informa como API no
     * disponible. Un 401 o 403 (clave inválida o cuota mensual agotada) también, con el detalle
     * en el log.
     */
    private RespuestaHttp get(String url) throws IOException {
        if (clave == null || clave.isBlank()) {
            LOG.severe("Falta la clave de REST Countries (api.properties o countryapi.restcountries.clave)");
            throw new IOException(NO_DISPONIBLE);
        }
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(TIMEOUT_SEGUNDOS * 1000);
            conexion.setReadTimeout(TIMEOUT_SEGUNDOS * 1000);
            conexion.setRequestProperty("Accept", "application/json");
            conexion.setRequestProperty("User-Agent", "CountryAPI/1.0");
            conexion.setRequestProperty("Authorization", "Bearer " + clave);
            int codigo = conexion.getResponseCode();
            InputStream entrada = codigo < 400 ? conexion.getInputStream() : conexion.getErrorStream();
            String cuerpo = leer(entrada);
            if (codigo == 401 || codigo == 403 || codigo == 429) {
                LOG.severe(() -> "REST Countries rechazó la petición (" + codigo + "): " + cuerpo);
            }
            return new RespuestaHttp(codigo, cuerpo);
        } catch (IOException | IllegalArgumentException e) {
            LOG.warning(() -> "No se pudo consultar REST Countries: " + e);
            throw new IOException(NO_DISPONIBLE, e);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static String leer(InputStream entrada) throws IOException {
        if (entrada == null) {
            return "";
        }
        try (InputStream in = entrada; ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            in.transferTo(salida);
            return salida.toString(StandardCharsets.UTF_8);
        }
    }

    /** Clave de la API: propiedad del sistema, variable de entorno o api.properties. */
    private static String leerClave() {
        String clave = System.getProperty("countryapi.restcountries.clave");
        if (clave == null || clave.isBlank()) {
            clave = System.getenv("COUNTRYAPI_RESTCOUNTRIES_CLAVE");
        }
        if (clave == null || clave.isBlank()) {
            try (InputStream entrada = ConsumoAPI.class.getClassLoader().getResourceAsStream("api.properties")) {
                if (entrada != null) {
                    Properties propiedades = new Properties();
                    propiedades.load(entrada);
                    clave = propiedades.getProperty("restcountries.clave");
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, "No se pudo leer api.properties", e);
            }
        }
        return clave == null ? null : clave.trim();
    }

    /** Código HTTP y cuerpo de una respuesta. */
    private record RespuestaHttp(int codigo, String cuerpo) {
    }

    /** Países convertidos de una página, cuántos objetos traía y si hay más páginas. */
    private record Pagina(List<Pais> paises, int recibidos, boolean hayMas) {
    }
}
