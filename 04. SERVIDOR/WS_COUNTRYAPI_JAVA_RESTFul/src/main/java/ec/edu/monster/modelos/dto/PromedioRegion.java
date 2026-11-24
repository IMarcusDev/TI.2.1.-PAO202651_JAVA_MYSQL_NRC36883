package ec.edu.monster.modelos.dto;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Fila del reporte «Promedio poblacional por región», redondeado a entero.
 *
 * @author Escobar Marcos
 */
@JsonbPropertyOrder({"region", "promedioPoblacion"})
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

    public void setRegion(String region) {
        this.region = region;
    }

    public long getPromedioPoblacion() {
        return promedioPoblacion;
    }

    public void setPromedioPoblacion(long promedioPoblacion) {
        this.promedioPoblacion = promedioPoblacion;
    }
}
