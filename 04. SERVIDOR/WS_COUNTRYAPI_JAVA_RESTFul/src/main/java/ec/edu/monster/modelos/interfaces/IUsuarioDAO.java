package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Usuario;
import java.sql.SQLException;

/**
 * Acceso a la tabla usuario.
 *
 * @author Escobar Marcos
 */
public interface IUsuarioDAO {

    /** Busca un usuario por su username; devuelve null si no existe. */
    Usuario buscarPorUsername(String username) throws SQLException;
}
