package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Universidad;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a la tabla universidad.
 *
 * @author Escobar Marcos
 */
public interface IUniversidadDAO {

    /**
     * Registra las universidades nuevas y actualiza las existentes (misma pareja nombre y país)
     * en una sola transacción. Si algo falla, deshace todo.
     */
    ResumenImportacion guardarTodas(List<Universidad> universidades) throws SQLException;

    /** Universidades guardadas de ese país, ordenadas por nombre. */
    List<Universidad> listarPorPais(String pais) throws SQLException;
}
