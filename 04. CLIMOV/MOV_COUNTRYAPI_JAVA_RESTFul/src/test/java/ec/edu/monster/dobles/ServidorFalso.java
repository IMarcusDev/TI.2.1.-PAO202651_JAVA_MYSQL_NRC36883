package ec.edu.monster.dobles;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servidor RESTful falso para probar servicios y controladores sin Payara ni MySQL.
 *
 * <p>Es la misma API que el servidor falso del cliente de escritorio, pero sobre un
 * {@link ServerSocket}: las pruebas locales de Android se compilan contra android.jar, que no
 * incluye com.sun.net.httpserver. Atiende una petición HTTP/1.1 por conexión.</p>
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

    private static final Respuesta SIN_CONFIGURAR =
            new Respuesta(404, "{\"estado\":\"ERROR\",\"mensaje\":\"Ruta no configurada en la prueba\",\"datos\":null}");

    private final ServerSocket servidor;
    private final Thread hilo;
    private final Map<String, Respuesta> respuestas = new ConcurrentHashMap<>();
    private final List<Peticion> peticiones = new CopyOnWriteArrayList<>();

    public ServidorFalso() throws IOException {
        servidor = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        hilo = new Thread(this::atender, "servidor-falso");
        hilo.setDaemon(true);
        hilo.start();
    }

    /** URL base equivalente a la del servidor real (termina en /api). */
    public String urlBase() {
        return "http://127.0.0.1:" + servidor.getLocalPort() + "/api";
    }

    /** Responde a «MÉTODO /ruta» con ese código y ese JSON. */
    public void responder(String metodoYRuta, int codigo, String json) {
        respuestas.put(metodoYRuta, new Respuesta(codigo, json));
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
        try {
            servidor.close();
        } catch (IOException e) {
            // Ya estaba cerrado.
        }
    }

    // ------------------------------------------------------------------ HTTP mínimo

    private void atender() {
        while (!servidor.isClosed()) {
            try (Socket conexion = servidor.accept()) {
                responderA(conexion);
            } catch (IOException e) {
                // Servidor cerrado o cliente desconectado: se sigue con la siguiente conexión.
            }
        }
    }

    private void responderA(Socket conexion) throws IOException {
        InputStream entrada = new BufferedInputStream(conexion.getInputStream());
        String[] inicio = leerLinea(entrada).split(" ");
        String metodo = inicio[0];
        String destino = inicio[1];
        String authorization = null;
        int largo = 0;
        for (String linea = leerLinea(entrada); !linea.isEmpty(); linea = leerLinea(entrada)) {
            int dosPuntos = linea.indexOf(':');
            String nombre = linea.substring(0, dosPuntos).trim().toLowerCase(Locale.ROOT);
            String valor = linea.substring(dosPuntos + 1).trim();
            if (nombre.equals("authorization")) {
                authorization = valor;
            } else if (nombre.equals("content-length")) {
                largo = Integer.parseInt(valor);
            }
        }
        String cuerpo = new String(entrada.readNBytes(largo), StandardCharsets.UTF_8);

        String sinApi = destino.substring("/api".length());
        int interrogacion = sinApi.indexOf('?');
        String ruta = interrogacion < 0 ? sinApi : sinApi.substring(0, interrogacion);
        peticiones.add(new Peticion(metodo, sinApi, authorization, cuerpo));
        Respuesta respuesta = respuestas.getOrDefault(metodo + " " + ruta, SIN_CONFIGURAR);

        byte[] bytes = respuesta.cuerpo().getBytes(StandardCharsets.UTF_8);
        String cabecera = "HTTP/1.1 " + respuesta.codigo() + " Prueba\r\n"
                + "Content-Type: application/json; charset=UTF-8\r\n"
                + "Content-Length: " + bytes.length + "\r\n"
                + "Connection: close\r\n\r\n";
        OutputStream salida = conexion.getOutputStream();
        salida.write(cabecera.getBytes(StandardCharsets.ISO_8859_1));
        salida.write(bytes);
        salida.flush();
    }

    /** Lee una línea terminada en CRLF (sin incluirlo). */
    private static String leerLinea(InputStream entrada) throws IOException {
        ByteArrayOutputStream linea = new ByteArrayOutputStream();
        int actual;
        while ((actual = entrada.read()) != -1) {
            if (actual == '\n') {
                break;
            }
            if (actual != '\r') {
                linea.write(actual);
            }
        }
        return linea.toString(StandardCharsets.ISO_8859_1);
    }
}
