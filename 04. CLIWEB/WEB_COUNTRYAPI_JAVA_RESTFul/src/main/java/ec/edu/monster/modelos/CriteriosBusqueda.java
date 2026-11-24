package ec.edu.monster.modelos;

/**
 * Criterios de «Buscar y filtrar»: nombre y capital (parciales) y región (exacta).
 * Un criterio vacío o null no se envía.
 *
 * @author Escobar Marcos
 */
public class CriteriosBusqueda {

    private String nombre;
    private String capital;
    private String region;

    public CriteriosBusqueda() {
    }

    public CriteriosBusqueda(String nombre, String capital, String region) {
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCapital() {
        return capital;
    }

    public String getRegion() {
        return region;
    }
}
