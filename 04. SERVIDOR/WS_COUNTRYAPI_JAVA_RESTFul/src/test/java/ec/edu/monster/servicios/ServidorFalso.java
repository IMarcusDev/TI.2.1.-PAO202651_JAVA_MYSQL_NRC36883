package ec.edu.monster.servicios;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Servidor HTTP local para probar el consumo de las APIs sin internet. Guarda las rutas pedidas
 * y el último encabezado Authorization.
 */
final class ServidorFalso implements AutoCloseable {

    private final HttpServer servidor;
    private volatile int codigo = 200;
    /** Cuerpo según la ruta pedida (ruta con su query). */
    private volatile Function<String, String> cuerpoSegunRuta = ruta -> "[]";
    private volatile String ubicacion;
    private final List<String> rutas = new CopyOnWriteArrayList<>();
    private volatile String ultimoAuthorization;

    ServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            String ruta = intercambio.getRequestURI().toString();
            rutas.add(ruta);
            ultimoAuthorization = intercambio.getRequestHeaders().getFirst("Authorization");
            if (ubicacion != null) {
                // Primera petición: redirección 301 sin cuerpo.
                intercambio.getResponseHeaders().add("Location", ubicacion);
                ubicacion = null;
                intercambio.sendResponseHeaders(301, -1);
                intercambio.close();
                return;
            }
            byte[] bytes = cuerpoSegunRuta.apply(ruta).getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            intercambio.sendResponseHeaders(codigo, bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();
    }

    String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    /** Responde siempre el mismo código y cuerpo. */
    void responder(int codigo, String cuerpo) {
        this.codigo = codigo;
        this.cuerpoSegunRuta = ruta -> cuerpo;
    }

    /** Responde 200 con un cuerpo que depende de la ruta (por ejemplo, según el offset). */
    void responderSegun(Function<String, String> cuerpoSegunRuta) {
        this.codigo = 200;
        this.cuerpoSegunRuta = cuerpoSegunRuta;
    }

    /** La próxima petición recibe un 301 hacia esa ubicación; las siguientes, la respuesta normal. */
    void redirigirUnaVez(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    String ultimaRuta() {
        return rutas.isEmpty() ? null : rutas.get(rutas.size() - 1);
    }

    List<String> rutas() {
        return rutas;
    }

    String ultimoAuthorization() {
        return ultimoAuthorization;
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
