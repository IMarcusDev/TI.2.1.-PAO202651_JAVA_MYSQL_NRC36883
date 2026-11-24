package ec.edu.monster.modelos.dto;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.util.List;

/**
 * Los tres reportes estadísticos en una sola respuesta (RQF-006): países por región,
 * país más poblado y promedio poblacional por región.
 *
 * @author Escobar Marcos
 */
@JsonbPropertyOrder({"paisesPorRegion", "paisMasPoblado", "promedioPorRegion"})
public class ReporteDTO {

    private List<ConteoRegion> paisesPorRegion;
    private PaisDTO paisMasPoblado;
    private List<PromedioRegion> promedioPorRegion;

    public ReporteDTO() {
    }

    public ReporteDTO(List<ConteoRegion> paisesPorRegion, PaisDTO paisMasPoblado,
            List<PromedioRegion> promedioPorRegion) {
        this.paisesPorRegion = paisesPorRegion;
        this.paisMasPoblado = paisMasPoblado;
        this.promedioPorRegion = promedioPorRegion;
    }

    public List<ConteoRegion> getPaisesPorRegion() {
        return paisesPorRegion;
    }

    public void setPaisesPorRegion(List<ConteoRegion> paisesPorRegion) {
        this.paisesPorRegion = paisesPorRegion;
    }

    public PaisDTO getPaisMasPoblado() {
        return paisMasPoblado;
    }

    public void setPaisMasPoblado(PaisDTO paisMasPoblado) {
        this.paisMasPoblado = paisMasPoblado;
    }

    public List<PromedioRegion> getPromedioPorRegion() {
        return promedioPorRegion;
    }

    public void setPromedioPorRegion(List<PromedioRegion> promedioPorRegion) {
        this.promedioPorRegion = promedioPorRegion;
    }
}
