package ec.edu.monster.servicios;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ec.edu.monster.config.ConfigServidor;
import ec.edu.monster.modelos.RespuestaDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Único punto del cliente que habla con el servidor RESTful (HttpURLConnection + Gson).
 *
 * <p>Siempre devuelve una {@link RespuestaDTO}, nunca lanza excepciones a los controladores:</p>
 * <ul>
 *   <li>si el servidor responde, su JSON {estado, mensaje, datos};</li>
 *   <li>si una petición con token recibe 401, un ERROR «Su sesión expiró o no es válida...»
 *       marcado como sesión expirada;</li>
 *   <li>si no hay conexión o se agota el tiempo, un ERROR «No se pudo conectar con el
 *       servidor...».</li>
 * </ul>
 *
 * @author Escobar Marcos
 */
public class ClienteRest {

    public static final String SIN_CONEXION =
            "No se pudo conectar con el servidor. Verifique la red e intente nuevamente.";
    public static final String SESION_EXPIRADA = "Su sesión expiró o no es válida. Inicie sesión nuevamente.";
    public static final String RESPUESTA_NO_VALIDA = "Ocurrió un error inesperado, intente nuevamente";

    private static final Logger LOG = Logger.getLogger(ClienteRest.class.getName());
    private static final int ESPERA_ESTADO_MS = 3000;

    /** Puede cambiar en tiempo de ejecución (pantalla de inicio de sesión → «Cambiar servidor»). */
    private volatile String urlBase;
    private final int timeoutMs;
    private final Gson gson = new Gson();

    /** Cliente apuntado a la dirección indicada, con el tiempo de espera de ConfigServidor. */
    public ClienteRest(String urlBase) {
        this(urlBase, ConfigServidor.TIMEOUT_MS);
    }

    public ClienteRest(String urlBase, int timeoutMs) {
        setUrlBase(urlBase);
        this.timeoutMs = timeoutMs;
    }

    /** Apunta el cliente a otro servidor (las peticiones siguientes ya usan la nueva dirección). */
    public final void setUrlBase(String urlBase) {
        this.urlBase = ConfigServidor.quitarBarraFinal(urlBase.trim());
    }

    /**
     * Envía una petición y convierte la respuesta.
     *
     * @param metodo GET, POST, PUT o DELETE
     * @param ruta   ruta desde /api, por ejemplo "/Paises/buscar?region=Europa"
     * @param token  token de la sesión, o null (inicio de sesión)
     * @param cuerpo objeto que se envía como JSON, o null
     * @param tipo   tipo de «datos», por ejemplo Pais.class o una lista
     */
    public <T> RespuestaDTO<T> enviar(String metodo, String ruta, String token, Object cuerpo, Type tipo) {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) URI.create(urlBase + ruta).toURL().openConnection();
            conexion.setRequestMethod(metodo);
            conexion.setConnectTimeout(timeoutMs);
            conexion.setReadTimeout(timeoutMs);
            conexion.setRequestProperty("Accept", "application/json");
            if (token != null) {
                conexion.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (cuerpo != null || "POST".equals(metodo) || "PUT".equals(metodo)) {
                // POST y PUT siempre llevan cuerpo (aunque vacío) para enviar Content-Length.
                byte[] bytes = cuerpo == null ? new byte[0] : gson.toJson(cuerpo).getBytes(StandardCharsets.UTF_8);
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(bytes);
                }
            }
            int codigo = conexion.getResponseCode();
            if (token != null && codigo == 401) {
                RespuestaDTO<T> expirada = RespuestaDTO.error(SESION_EXPIRADA);
                expirada.setCodigoHttp(codigo);
                expirada.setSesionExpirada(true);
                return expirada;
            }
            return convertir(codigo, leerRespuesta(conexion), tipo);
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.FINE, "Sin conexión con " + urlBase + ruta, e);
            return RespuestaDTO.error(SIN_CONEXION);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    /** True si el servidor responde ahora mismo (cualquier código menor que 500). */
    public boolean servidorDisponible() {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) URI.create(urlBase + "/Paises").toURL().openConnection();
            conexion.setConnectTimeout(ESPERA_ESTADO_MS);
            conexion.setReadTimeout(ESPERA_ESTADO_MS);
            return conexion.getResponseCode() < 500;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    public String getUrlBase() {
        return urlBase;
    }

    /** Codifica un valor para la URL (query o ruta): los espacios quedan como %20. */
    public static String codificar(String valor) {
        try {
            // Versión con nombre de juego de caracteres: la de Charset no existe en Android 7.
            return URLEncoder.encode(valor, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 siempre está disponible", e);
        }
    }

    private <T> RespuestaDTO<T> convertir(int codigo, String json, Type tipo) {
        try {
            Type tipoRespuesta = TypeToken.getParameterized(RespuestaDTO.class, tipo).getType();
            RespuestaDTO<T> respuesta = gson.fromJson(json, tipoRespuesta);
            if (respuesta == null || respuesta.getEstado() == null) {
                // Logger.warning(Supplier) no existe en Android 7: se arma el texto directamente.
                LOG.warning("Respuesta sin formato estructurado (HTTP " + codigo + ")");
                respuesta = RespuestaDTO.error(RESPUESTA_NO_VALIDA);
            }
            respuesta.setCodigoHttp(codigo);
            return respuesta;
        } catch (JsonParseException e) {
            LOG.warning("JSON no válido del servidor (HTTP " + codigo + "): " + e.getMessage());
            RespuestaDTO<T> error = RespuestaDTO.error(RESPUESTA_NO_VALIDA);
            error.setCodigoHttp(codigo);
            return error;
        }
    }

    /** Lee el cuerpo de la respuesta en UTF-8 (o el de error si el código es 400 o más). */
    private String leerRespuesta(HttpURLConnection conexion) throws IOException {
        InputStream entrada = conexion.getResponseCode() < 400 ? conexion.getInputStream() : conexion.getErrorStream();
        if (entrada == null) {
            return "";
        }
        // Copia manual (InputStream.transferTo no existe en Android 7).
        try (InputStream in = entrada; ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            byte[] bufer = new byte[8192];
            int leidos;
            while ((leidos = in.read(bufer)) != -1) {
                salida.write(bufer, 0, leidos);
            }
            return new String(salida.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
