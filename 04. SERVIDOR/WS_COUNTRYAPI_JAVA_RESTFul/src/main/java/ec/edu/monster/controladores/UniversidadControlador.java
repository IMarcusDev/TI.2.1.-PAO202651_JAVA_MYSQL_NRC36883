package ec.edu.monster.controladores;

import ec.edu.monster.modelos.data.Universidad;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.interfaces.IUniversidadDAO;
import ec.edu.monster.modelos.repositorios.UniversidadDAO;
import ec.edu.monster.servicios.ConsumoAPIUniversidades;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Importación y consulta de universidades (RQF-010, ECUD10).
 *
 * @author Escobar Marcos
 */
public class UniversidadControlador {

    static final String INGRESE_PAIS = "Ingrese el país";
    static final String SIN_UNIVERSIDADES = "No se encontraron universidades para ese país";

    private final IUniversidadDAO universidadDAO;
    private final ConsumoAPIUniversidades consumoAPI;

    public UniversidadControlador() {
        this(new UniversidadDAO(), new ConsumoAPIUniversidades());
    }

    public UniversidadControlador(IUniversidadDAO universidadDAO, ConsumoAPIUniversidades consumoAPI) {
        this.universidadDAO = universidadDAO;
        this.consumoAPI = consumoAPI;
    }

    /**
     * Consulta la API con el país (en inglés) y registra o actualiza sus universidades en una
     * transacción. Si la API falla o no devuelve universidades, la base no cambia.
     */
    public ResumenImportacion importarUniversidades(String pais) throws IOException, SQLException {
        validarPais(pais);
        List<Universidad> universidades = consumoAPI.obtenerPorPais(pais);
        if (universidades.isEmpty()) {
            throw new NoSuchElementException(SIN_UNIVERSIDADES);
        }
        return universidadDAO.guardarTodas(universidades);
    }

    /** Universidades guardadas del país, ordenadas por nombre (lista vacía si no hay). */
    public List<Universidad> listarUniversidades(String pais) throws SQLException {
        validarPais(pais);
        return universidadDAO.listarPorPais(pais);
    }

    private static void validarPais(String pais) {
        if (pais == null || pais.isBlank()) {
            throw new IllegalArgumentException(INGRESE_PAIS);
        }
    }
}
