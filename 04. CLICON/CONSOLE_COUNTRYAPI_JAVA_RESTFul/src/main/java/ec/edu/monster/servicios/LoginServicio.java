package ec.edu.monster.servicios;

import ec.edu.monster.modelos.LoginRequest;
import ec.edu.monster.modelos.LoginResponse;
import ec.edu.monster.modelos.RespuestaDTO;

/**
 * Inicio y cierre de sesión: /Login/login y /Login/logout.
 *
 * @author Escobar Marcos
 */
public class LoginServicio {

    private final ClienteRest cliente;

    public LoginServicio(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public RespuestaDTO<LoginResponse> login(String usuario, String clave) {
        return cliente.enviar("POST", "/Login/login", null, new LoginRequest(usuario, clave), LoginResponse.class);
    }

    public RespuestaDTO<Void> logout(String token) {
        return cliente.enviar("POST", "/Login/logout", token, null, Void.class);
    }
}
