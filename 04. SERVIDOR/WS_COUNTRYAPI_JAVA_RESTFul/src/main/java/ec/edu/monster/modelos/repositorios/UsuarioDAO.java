package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.DBConfig;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.enums.EstadoUsuario;
import ec.edu.monster.modelos.interfaces.IUsuarioDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO de la tabla usuario (JDBC con consultas parametrizadas).
 *
 * @author Escobar Marcos
 */
public class UsuarioDAO implements IUsuarioDAO {

    private static final String SQL_POR_USERNAME =
            "SELECT id_usuario, username, password_hash, nombre, estado FROM usuario WHERE username = ?";

    @Override
    public Usuario buscarPorUsername(String username) throws SQLException {
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement(SQL_POR_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("nombre"),
                        EstadoUsuario.valueOf(rs.getString("estado")));
            }
        }
    }
}
