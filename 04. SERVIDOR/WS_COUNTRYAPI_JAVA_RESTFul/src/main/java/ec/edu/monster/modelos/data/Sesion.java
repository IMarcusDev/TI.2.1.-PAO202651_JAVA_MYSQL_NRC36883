package ec.edu.monster.modelos.data;

import java.time.LocalDateTime;

/**
 * Sesión registrada en memoria por TokenService: el token, su usuario y la hora del último uso
 * (base para la expiración por inactividad de 15 minutos).
 *
 * @author Escobar Marcos
 */
public class Sesion {

    private String token;
    private Usuario usuario;
    private LocalDateTime ultimoUso;

    public Sesion() {
    }

    public Sesion(String token, Usuario usuario, LocalDateTime ultimoUso) {
        this.token = token;
        this.usuario = usuario;
        this.ultimoUso = ultimoUso;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getUltimoUso() {
        return ultimoUso;
    }

    public void setUltimoUso(LocalDateTime ultimoUso) {
        this.ultimoUso = ultimoUso;
    }
}
