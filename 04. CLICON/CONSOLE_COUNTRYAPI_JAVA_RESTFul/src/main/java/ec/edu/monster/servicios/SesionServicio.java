package ec.edu.monster.servicios;

/**
 * Datos de la sesión abierta: el token que entrega el servidor y el nombre del usuario.
 * Solo vive en memoria mientras la aplicación está abierta.
 *
 * @author Escobar Marcos
 */
public class SesionServicio {

    private volatile String token;
    private volatile String nombre;

    public void guardar(String token, String nombre) {
        this.token = token;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean estaActiva() {
        return token != null;
    }

    public void cerrar() {
        token = null;
        nombre = null;
    }
}
