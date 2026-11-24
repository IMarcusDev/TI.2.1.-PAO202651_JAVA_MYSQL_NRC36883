package ec.edu.monster.modelos;

/**
 * Respuesta estructurada del servidor: {"estado", "mensaje", "datos"}.
 *
 * <p>Además guarda el código HTTP (no viene en el JSON) para que los controladores sepan si la
 * sesión expiró (401 con token) y deban volver al inicio de sesión.</p>
 *
 * @param <T> tipo de los datos
 * @author Escobar Marcos
 */
public class RespuestaDTO<T> {

    private EstadoRespuesta estado;
    private String mensaje;
    private T datos;
    /** Código HTTP de la respuesta; 0 si no hubo conexión. No se lee del JSON. */
    private transient int codigoHttp;
    /** True cuando el servidor rechazó el token de la sesión. */
    private transient boolean sesionExpirada;

    public RespuestaDTO() {
    }

    public RespuestaDTO(EstadoRespuesta estado, String mensaje, T datos) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    /** Respuesta de error creada por el propio cliente (por ejemplo, sin conexión). */
    public static <T> RespuestaDTO<T> error(String mensaje) {
        return new RespuestaDTO<>(EstadoRespuesta.ERROR, mensaje, null);
    }

    public boolean esOk() {
        return estado == EstadoRespuesta.OK;
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

    public int getCodigoHttp() {
        return codigoHttp;
    }

    public void setCodigoHttp(int codigoHttp) {
        this.codigoHttp = codigoHttp;
    }

    public boolean isSesionExpirada() {
        return sesionExpirada;
    }

    public void setSesionExpirada(boolean sesionExpirada) {
        this.sesionExpirada = sesionExpirada;
    }
}
