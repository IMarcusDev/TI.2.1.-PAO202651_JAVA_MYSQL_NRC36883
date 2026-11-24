package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadoRespuesta;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * Respuesta estructurada de todas las operaciones del servidor (RQF-009, ECUD9):
 * {"estado": "OK" | "ERROR", "mensaje": "...", "datos": ...}.
 *
 * <p>En una respuesta con error, datos va en null. Además del formato, esta clase decide qué
 * código HTTP corresponde a cada error, según la excepción que lanzó el controlador:</p>
 * <ul>
 *   <li>IllegalArgumentException → 400 (datos inválidos)</li>
 *   <li>SecurityException → 401 (credenciales o token)</li>
 *   <li>NoSuchElementException → 404 (recurso inexistente)</li>
 *   <li>IllegalStateException → 409 (país repetido)</li>
 *   <li>IOException / UncheckedIOException → 502 (falla de una API externa)</li>
 *   <li>SQLException → 500 «Servicio no disponible, intente más tarde»</li>
 *   <li>cualquier otra → 500 «Ocurrió un error inesperado, intente nuevamente»</li>
 * </ul>
 * Así el mensaje nunca expone SQL ni trazas: para los 500 se usa un texto fijo.
 *
 * @param <T> tipo de los datos de la operación
 * @author Escobar Marcos
 */
@JsonbNillable
@JsonbPropertyOrder({"estado", "mensaje", "datos"})
public class RespuestaDTO<T> {

    public static final String TOKEN_INVALIDO = "Token inválido o sesión expirada";
    public static final String SERVICIO_NO_DISPONIBLE = "Servicio no disponible, intente más tarde";
    public static final String ERROR_INESPERADO = "Ocurrió un error inesperado, intente nuevamente";

    private EstadoRespuesta estado;
    private String mensaje;
    private T datos;

    public RespuestaDTO() {
    }

    public RespuestaDTO(EstadoRespuesta estado, String mensaje, T datos) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    /** Respuesta correcta con su mensaje y sus datos. */
    public static <T> RespuestaDTO<T> ok(String mensaje, T datos) {
        return new RespuestaDTO<>(EstadoRespuesta.OK, mensaje, datos);
    }

    /** Respuesta de error: los datos quedan vacíos. */
    public static <T> RespuestaDTO<T> error(String mensaje) {
        return new RespuestaDTO<>(EstadoRespuesta.ERROR, mensaje, null);
    }

    /** Convierte la excepción de un controlador en la respuesta de error que verá el cliente. */
    public static <T> RespuestaDTO<T> deExcepcion(Throwable e) {
        return error(mensajeDe(e));
    }

    /** Código HTTP que acompaña a la respuesta de error de esa excepción. */
    public static int codigoHttp(Throwable e) {
        if (e instanceof IllegalArgumentException) {
            return 400;
        }
        if (e instanceof SecurityException) {
            return 401;
        }
        if (e instanceof NoSuchElementException) {
            return 404;
        }
        if (e instanceof IllegalStateException) {
            return 409;
        }
        if (e instanceof IOException || e instanceof UncheckedIOException) {
            return 502;
        }
        return 500;
    }

    /** True si el error no es de negocio y conviene dejar el detalle en el log del servidor. */
    public static boolean esErrorInterno(Throwable e) {
        return codigoHttp(e) == 500;
    }

    private static String mensajeDe(Throwable e) {
        if (e instanceof SQLException) {
            return SERVICIO_NO_DISPONIBLE;
        }
        if (codigoHttp(e) == 500 || e.getMessage() == null || e.getMessage().isBlank()) {
            return ERROR_INESPERADO;
        }
        return e.getMessage();
    }

    public EstadoRespuesta getEstado() {
        return estado;
    }

    public void setEstado(EstadoRespuesta estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getDatos() {
        return datos;
    }

    public void setDatos(T datos) {
        this.datos = datos;
    }
}
