package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.DBConfig;
import ec.edu.monster.modelos.data.Universidad;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.interfaces.IUniversidadDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * DAO de la tabla universidad. Todas las sentencias son parametrizadas (PreparedStatement).
 *
 * @author Escobar Marcos
 */
public class UniversidadDAO implements IUniversidadDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO universidad (nombre, dominio, sitio_web, pais) VALUES (?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE universidad SET dominio = ?, sitio_web = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";

    /**
     * Importación de las universidades de un país (RQF-010):
     * <ol>
     *   <li>carga una sola vez las universidades que ya existen de los países recibidos;</li>
     *   <li>separa cada universidad en un lote de INSERT (nueva) o de UPDATE (misma pareja
     *       nombre y país: se actualizan dominio, sitio web y fecha);</li>
     *   <li>ejecuta los lotes con executeBatch dentro de una sola transacción.</li>
     * </ol>
     * Si algo falla se hace ROLLBACK.
     */
    @Override
    public ResumenImportacion guardarTodas(List<Universidad> universidades) throws SQLException {
        int registradas = 0;
        int actualizadas = 0;
        try (Connection cn = DBConfig.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psInsertar = cn.prepareStatement(SQL_INSERTAR);
                    PreparedStatement psActualizar = cn.prepareStatement(SQL_ACTUALIZAR)) {
                Map<String, Integer> existentes = idsExistentes(cn, universidades);
                Set<String> procesadas = new HashSet<>();
                for (Universidad u : universidades) {
                    String clave = clave(u.getNombre(), u.getPais());
                    if (!procesadas.add(clave)) {
                        continue;
                    }
                    Integer id = existentes.get(clave);
                    if (id == null) {
                        psInsertar.setString(1, u.getNombre());
                        psInsertar.setString(2, u.getDominio());
                        psInsertar.setString(3, u.getSitioWeb());
                        psInsertar.setString(4, u.getPais());
                        psInsertar.addBatch();
                        registradas++;
                    } else {
                        psActualizar.setString(1, u.getDominio());
                        psActualizar.setString(2, u.getSitioWeb());
                        psActualizar.setInt(3, id);
                        psActualizar.addBatch();
                        actualizadas++;
                    }
                }
                psInsertar.executeBatch();
                psActualizar.executeBatch();
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return new ResumenImportacion(universidades.size(), registradas, actualizadas);
    }

    @Override
    public List<Universidad> listarPorPais(String pais) throws SQLException {
        String sql = "SELECT id, nombre, dominio, sitio_web, pais, fecha_actualizacion"
                + " FROM universidad WHERE pais = ? ORDER BY nombre";
        List<Universidad> lista = new ArrayList<>();
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, pais.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Convierte la fila actual del ResultSet en una Universidad. */
    private Universidad mapear(ResultSet rs) throws SQLException {
        Universidad u = new Universidad(
                rs.getString("nombre"),
                rs.getString("dominio"),
                rs.getString("sitio_web"),
                rs.getString("pais"));
        u.setId(rs.getInt("id"));
        u.setFechaActualizacion(rs.getObject("fecha_actualizacion", LocalDateTime.class));
        return u;
    }

    /** id de las universidades existentes de los países recibidos, por clave nombre|país. */
    private static Map<String, Integer> idsExistentes(Connection cn, List<Universidad> universidades)
            throws SQLException {
        Set<String> paises = new HashSet<>();
        for (Universidad u : universidades) {
            paises.add(u.getPais());
        }
        Map<String, Integer> ids = new HashMap<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT id, nombre, pais FROM universidad WHERE pais = ?")) {
            for (String pais : paises) {
                ps.setString(1, pais);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ids.put(clave(rs.getString("nombre"), rs.getString("pais")), rs.getInt("id"));
                    }
                }
            }
        }
        return ids;
    }

    /** Clave nombre|país sin tildes ni mayúsculas, como el índice UNIQUE de MySQL (ai_ci). */
    private static String clave(String nombre, String pais) {
        return normalizar(nombre) + "|" + normalizar(pais);
    }

    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }
}
