package ec.edu.monster.modelos.dto;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Fila del reporte «Cantidad de países por región».
 *
 * @author Escobar Marcos
 */
@JsonbPropertyOrder({"region", "cantidad"})
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

    public void setRegion(String region) {
        this.region = region;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
