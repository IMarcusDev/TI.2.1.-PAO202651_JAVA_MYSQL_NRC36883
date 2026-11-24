package ec.edu.monster.modelos.dto;

/**
 * Cuerpo de POST /api/Login/login: {"usuario": "...", "clave": "..."}.
 *
 * @author Escobar Marcos
 */
public class LoginRequestDTO {

    private String usuario;
    private String clave;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String usuario, String clave) {
        this.usuario = usuario;
        this.clave = clave;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
