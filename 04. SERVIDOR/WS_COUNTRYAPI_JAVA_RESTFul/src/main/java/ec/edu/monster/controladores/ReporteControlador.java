package ec.edu.monster.controladores;

import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.dto.ConteoRegion;
import ec.edu.monster.modelos.dto.PaisDTO;
import ec.edu.monster.modelos.dto.ReporteDTO;
import ec.edu.monster.modelos.interfaces.IPaisDAO;
import ec.edu.monster.modelos.repositorios.PaisDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Reportes estadísticos sobre los países almacenados (RQF-006, ECUD6).
 *
 * @author Escobar Marcos
 */
public class ReporteControlador {

    static final String SIN_PAISES = "No hay países registrados para generar reportes";

    private final IPaisDAO paisDAO;

    public ReporteControlador() {
        this(new PaisDAO());
    }

    public ReporteControlador(IPaisDAO paisDAO) {
        this.paisDAO = paisDAO;
    }

    /**
     * Calcula, con consultas de agregación, la cantidad de países por región, el país más
     * poblado y el promedio poblacional por región.
     *
     * @throws NoSuchElementException si no hay países almacenados (404)
     */
    public ReporteDTO generarReportes() throws SQLException {
        List<ConteoRegion> paisesPorRegion = paisDAO.contarPorRegion();
        int total = paisesPorRegion.stream().mapToInt(ConteoRegion::getCantidad).sum();
        if (total == 0) {
            throw new NoSuchElementException(SIN_PAISES);
        }
        Pais masPoblado = paisDAO.paisMasPoblado();
        return new ReporteDTO(
                paisesPorRegion,
                masPoblado == null ? null : PaisDTO.desde(masPoblado, true),
                paisDAO.promedioPoblacionPorRegion());
    }
}
