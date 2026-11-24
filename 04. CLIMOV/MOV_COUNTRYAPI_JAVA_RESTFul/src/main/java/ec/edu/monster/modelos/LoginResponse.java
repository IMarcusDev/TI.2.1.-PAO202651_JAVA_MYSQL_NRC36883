package ec.edu.monster.modelos;

/**
 * Datos de un inicio de sesión correcto: token y nombre del usuario.
 *
 * @author Escobar Marcos
 */
public class LoginResponse {

    private String token;
    private String nombre;

    public LoginResponse() {
    }

    public LoginResponse(String token, String nombre) {
        this.token = token;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }
}
