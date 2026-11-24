package ec.edu.monster.modelos;

/**
 * Universidad guardada: nombre, dominio, sitio web y país.
 *
 * @author Escobar Marcos
 */
public class Universidad {

    private String nombre;
    private String dominio;
    private String sitioWeb;
    private String pais;

    public Universidad() {
    }

    public Universidad(String nombre, String dominio, String sitioWeb, String pais) {
        this.nombre = nombre;
        this.dominio = dominio;
        this.sitioWeb = sitioWeb;
        this.pais = pais;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDominio() {
        return dominio;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public String getPais() {
        return pais;
    }
}
