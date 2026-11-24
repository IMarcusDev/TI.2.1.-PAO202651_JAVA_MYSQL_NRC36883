package ec.edu.monster.modelos;

/**
 * Fila del reporte «Promedio poblacional por región».
 *
 * @author Escobar Marcos
 */
public class PromedioRegion {

    private String region;
    private long promedioPoblacion;

    public PromedioRegion() {
    }

    public PromedioRegion(String region, long promedioPoblacion) {
        this.region = region;
        this.promedioPoblacion = promedioPoblacion;
    }

    public String getRegion() {
        return region;
    }

    public long getPromedioPoblacion() {
        return promedioPoblacion;
    }
}
