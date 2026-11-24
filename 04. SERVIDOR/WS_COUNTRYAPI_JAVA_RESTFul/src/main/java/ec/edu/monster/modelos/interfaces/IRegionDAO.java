package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Region;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso al catálogo region.
 *
 * @author Escobar Marcos
 */
public interface IRegionDAO {

    /** Región con ese nombre, o null si no existe. */
    Region buscarPorNombre(String nombre) throws SQLException;

    /** Las seis regiones del catálogo. */
    List<Region> listar() throws SQLException;
}
