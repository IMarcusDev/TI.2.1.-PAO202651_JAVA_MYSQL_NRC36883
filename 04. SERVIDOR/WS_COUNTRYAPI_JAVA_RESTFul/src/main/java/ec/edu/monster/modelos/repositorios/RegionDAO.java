package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.DBConfig;
import ec.edu.monster.modelos.data.Region;
import ec.edu.monster.modelos.interfaces.IRegionDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO del catálogo region. La comparación por nombre usa la intercalación de la base
 * (utf8mb4_0900_ai_ci), que no distingue mayúsculas ni tildes.
 *
 * @author Escobar Marcos
 */
public class RegionDAO implements IRegionDAO {

    private static final String SQL_POR_NOMBRE = "SELECT id_region, nombre FROM region WHERE nombre = ?";
    private static final String SQL_LISTAR = "SELECT id_region, nombre FROM region ORDER BY id_region";

    @Override
    public Region buscarPorNombre(String nombre) throws SQLException {
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement(SQL_POR_NOMBRE)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Region(rs.getInt("id_region"), rs.getString("nombre")) : null;
            }
        }
    }

    @Override
    public List<Region> listar() throws SQLException {
        List<Region> regiones = new ArrayList<>();
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                regiones.add(new Region(rs.getInt("id_region"), rs.getString("nombre")));
            }
        }
        return regiones;
    }
}
