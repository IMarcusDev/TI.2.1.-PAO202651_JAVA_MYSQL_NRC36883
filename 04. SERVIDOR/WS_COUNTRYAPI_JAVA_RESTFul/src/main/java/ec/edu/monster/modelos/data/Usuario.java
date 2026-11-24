package ec.edu.monster.modelos.data;

import ec.edu.monster.modelos.enums.EstadoUsuario;

/**
 * Fila de la tabla usuario. La clave se guarda como hash SHA-256 (passwordHash).
 *
 * @author Escobar Marcos
 */
public class Usuario {

    private int id;
    private String username;
    private String passwordHash;
    private String nombre;
    private EstadoUsuario estado;

    public Usuario() {
    }

    public Usuario(int id, String username, String passwordHash, String nombre, EstadoUsuario estado) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }
}
