package ec.edu.monster.modelos.data;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.time.LocalDateTime;

/**
 * Fila de la tabla universidad, importada desde la API de universidades (Hipolabs).
 * La pareja nombre y país es única.
 *
 * @author Escobar Marcos
 */
@JsonbPropertyOrder({"id", "nombre", "dominio", "sitioWeb", "pais", "fechaActualizacion"})
public class Universidad {

    private int id;
    private String nombre;
    private String dominio;
    private String sitioWeb;
    private String pais;
    private LocalDateTime fechaActualizacion;

    public Universidad() {
    }

    public Universidad(String nombre, String dominio, String sitioWeb, String pais) {
        this.nombre = nombre;
        this.dominio = dominio;
        this.sitioWeb = sitioWeb;
        this.pais = pais;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
