package ec.edu.monster.modelos.dto;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Datos de un inicio de sesión correcto: el token y el nombre del usuario.
 *
 * @author Escobar Marcos
 */
@JsonbPropertyOrder({"token", "nombre"})
public class LoginResponseDTO {

    private String token;
    private String nombre;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String nombre) {
        this.token = token;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
