package ec.edu.monster.modelos;

import java.util.List;

/**
 * Los tres reportes estadísticos: países por región, país más poblado y promedio por región.
 *
 * @author Escobar Marcos
 */
public class Reporte {

    private List<ConteoRegion> paisesPorRegion;
    private Pais paisMasPoblado;
    private List<PromedioRegion> promedioPorRegion;

    public Reporte() {
    }

    public Reporte(List<ConteoRegion> paisesPorRegion, Pais paisMasPoblado, List<PromedioRegion> promedioPorRegion) {
        this.paisesPorRegion = paisesPorRegion;
        this.paisMasPoblado = paisMasPoblado;
        this.promedioPorRegion = promedioPorRegion;
    }

    public List<ConteoRegion> getPaisesPorRegion() {
        return paisesPorRegion;
    }

    public Pais getPaisMasPoblado() {
        return paisMasPoblado;
    }

    public List<PromedioRegion> getPromedioPorRegion() {
        return promedioPorRegion;
    }
}
