package ec.edu.monster.dobles;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servidor RESTful falso para probar servicios y controladores sin Payara ni MySQL.
 *
 * <p>Cada respuesta se registra con «MÉTODO /ruta» (la ruta sin la query, desde /api). Las
 * peticiones recibidas quedan guardadas para comprobar qué envió el cliente.</p>
 */
public final class ServidorFalso implements AutoCloseable {

    /** Petición recibida: método, ruta con query, encabezado Authorization y cuerpo. */
    public record Peticion(String metodo, String ruta, String authorization, String cuerpo) {
    }

    private record Respuesta(int codigo, String cuerpo) {
    }

    private final HttpServer servidor;
    private final Map<String, Respuesta> respuestas = new ConcurrentHashMap<>();
    /** Respuestas de un solo uso: se entregan antes que la fija de la misma ruta. */
    private final Map<String, Respuesta> primeras = new ConcurrentHashMap<>();
    private final List<Peticion> peticiones = new CopyOnWriteArrayList<>();

    public ServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/api", intercambio -> {
            String ruta = intercambio.getRequestURI().getRawPath().substring("/api".length());
            String query = intercambio.getRequestURI().getRawQuery();
            String cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            peticiones.add(new Peticion(intercambio.getRequestMethod(), query == null ? ruta : ruta + "?" + query,
                    intercambio.getRequestHeaders().getFirst("Authorization"), cuerpo));
            String clave = intercambio.getRequestMethod() + " " + ruta;
            Respuesta primera = primeras.remove(clave);
            Respuesta r = primera != null ? primera : respuestas.getOrDefault(clave,
                    new Respuesta(404, "{\"estado\":\"ERROR\",\"mensaje\":\"Ruta no configurada en la prueba\",\"datos\":null}"));
            byte[] bytes = r.cuerpo().getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            intercambio.sendResponseHeaders(r.codigo(), bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();
    }

    /** URL base equivalente a la del servidor real (termina en /api). */
    public String urlBase() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/api";
    }

    /** Responde a «MÉTODO /ruta» con ese código y ese JSON. */
    public void responder(String metodoYRuta, int codigo, String json) {
        respuestas.put(metodoYRuta, new Respuesta(codigo, json));
    }

    /** La próxima petición a «MÉTODO /ruta» recibe esta respuesta; las siguientes, la fija. */
    public void alPrimerPedido(String metodoYRuta, int codigo, String json) {
        primeras.put(metodoYRuta, new Respuesta(codigo, json));
    }

    /** Atajo para una respuesta OK con su mensaje y sus datos (JSON). */
    public void ok(String metodoYRuta, String mensaje, String datosJson) {
        responder(metodoYRuta, 200, "{\"estado\":\"OK\",\"mensaje\":\"" + mensaje + "\",\"datos\":" + datosJson + "}");
    }

    /** Atajo para una respuesta de error con su código y su mensaje. */
    public void error(String metodoYRuta, int codigo, String mensaje) {
        responder(metodoYRuta, codigo, "{\"estado\":\"ERROR\",\"mensaje\":\"" + mensaje + "\",\"datos\":null}");
    }

    public List<Peticion> peticiones() {
        return peticiones;
    }

    public Peticion ultima() {
        return peticiones.isEmpty() ? null : peticiones.get(peticiones.size() - 1);
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
