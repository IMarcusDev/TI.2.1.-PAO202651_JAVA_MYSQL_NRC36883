package ec.edu.monster.modelos.data;

/**
 * Fila del catálogo region (América, Europa, Asia, África, Oceanía, Antártida).
 *
 * @author Escobar Marcos
 */
public class Region {

    private int id;
    private String nombre;

    public Region() {
    }

    public Region(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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
}
