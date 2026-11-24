package ec.edu.monster.modelos;

/**
 * Fila del reporte «Cantidad de países por región».
 *
 * @author Escobar Marcos
 */
public class ConteoRegion {

    private String region;
    private int cantidad;

    public ConteoRegion() {
    }

    public ConteoRegion(String region, int cantidad) {
        this.region = region;
        this.cantidad = cantidad;
    }

    public String getRegion() {
        return region;
    }

    public int getCantidad() {
        return cantidad;
    }
}
