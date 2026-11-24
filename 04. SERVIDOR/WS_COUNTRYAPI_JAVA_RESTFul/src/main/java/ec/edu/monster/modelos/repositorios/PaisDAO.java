package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.DBConfig;
import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.data.Region;
import ec.edu.monster.modelos.dto.ConteoRegion;
import ec.edu.monster.modelos.dto.PromedioRegion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.interfaces.IPaisDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * DAO de la tabla pais. Todas las sentencias son parametrizadas (PreparedStatement).
 *
 * <p>Las búsquedas usan LIKE sobre la intercalación utf8mb4_0900_ai_ci de la base, así que
 * no distinguen mayúsculas ni tildes: «peru» encuentra «Perú».</p>
 *
 * @author Escobar Marcos
 */
public class PaisDAO implements IPaisDAO {

    /** Columnas de pais más el nombre de su región, para armar objetos Pais completos. */
    private static final String SQL_BASE =
            "SELECT p.id, p.nombre, p.capital, p.id_region, r.nombre AS region, p.subregion,"
            + " p.poblacion, p.area, p.moneda, p.fecha_actualizacion"
            + " FROM pais p JOIN region r ON r.id_region = p.id_region";

    private static final String SQL_INSERTAR =
            "INSERT INTO pais (nombre, capital, id_region, subregion, poblacion, area, moneda)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** Actualización completa desde el CRUD, incluido el nombre. */
    private static final String SQL_ACTUALIZAR =
            "UPDATE pais SET nombre = ?, capital = ?, id_region = ?, subregion = ?, poblacion = ?,"
            + " area = ?, moneda = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";

    /** Actualización desde la API: el país se identifica por nombre, que no cambia (ECUD2 paso 7). */
    private static final String SQL_ACTUALIZAR_DATOS_API =
            "UPDATE pais SET capital = ?, id_region = ?, subregion = ?, poblacion = ?, area = ?,"
            + " moneda = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";

    @Override
    public List<Pais> listar() throws SQLException {
        return consultar(SQL_BASE + " ORDER BY p.nombre", List.of());
    }

    @Override
    public List<Pais> buscarPorNombre(String texto) throws SQLException {
        return consultar(SQL_BASE + " WHERE p.nombre LIKE ? ORDER BY p.nombre", List.of(contiene(texto)));
    }

    @Override
    public List<Pais> buscar(String nombre, String capital, Integer idRegion) throws SQLException {
        // Solo se agregan al WHERE los criterios que tienen valor; los valores van como parámetros.
        StringBuilder sql = new StringBuilder(SQL_BASE).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (nombre != null && !nombre.isBlank()) {
            sql.append(" AND p.nombre LIKE ?");
            parametros.add(contiene(nombre));
        }
        if (capital != null && !capital.isBlank()) {
            sql.append(" AND p.capital LIKE ?");
            parametros.add(contiene(capital));
        }
        if (idRegion != null) {
            sql.append(" AND p.id_region = ?");
            parametros.add(idRegion);
        }
        sql.append(" ORDER BY p.nombre");
        return consultar(sql.toString(), parametros);
    }

    @Override
    public Pais buscarPorId(int id) throws SQLException {
        List<Pais> encontrados = consultar(SQL_BASE + " WHERE p.id = ?", List.of(id));
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    @Override
    public boolean existePorNombre(String nombre) throws SQLException {
        try (Connection cn = DBConfig.getConnection()) {
            return buscarIdPorNombre(cn, nombre) != null;
        }
    }

    @Override
    public int insertar(Pais pais) throws SQLException {
        try (Connection cn = DBConfig.getConnection()) {
            return insertar(cn, pais);
        }
    }

    @Override
    public boolean actualizar(Pais pais) throws SQLException {
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, pais.getNombre());
            ps.setString(2, pais.getCapital());
            ps.setInt(3, pais.getRegion().getId());
            ps.setString(4, pais.getSubregion());
            ps.setLong(5, pais.getPoblacion());
            ps.setDouble(6, pais.getArea());
            ps.setString(7, pais.getMoneda());
            ps.setInt(8, pais.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement("DELETE FROM pais WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Importación masiva (RQF-002, RNF-006):
     * <ol>
     *   <li>carga una sola vez los países que ya existen (id por nombre);</li>
     *   <li>separa cada país recibido en un lote de INSERT (nuevo) o de UPDATE (existente);</li>
     *   <li>ejecuta los dos lotes con executeBatch dentro de una sola transacción.</li>
     * </ol>
     * Si cualquier sentencia falla se hace ROLLBACK y la base queda como estaba.
     * Los nombres se comparan sin mayúsculas ni tildes, igual que el índice UNIQUE de MySQL,
     * y si la API trae dos países con el mismo nombre solo se guarda el primero.
     */
    @Override
    public ResumenImportacion guardarTodos(List<Pais> paises) throws SQLException {
        int registrados = 0;
        int actualizados = 0;
        try (Connection cn = DBConfig.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psInsertar = cn.prepareStatement(SQL_INSERTAR);
                    PreparedStatement psActualizar = cn.prepareStatement(SQL_ACTUALIZAR_DATOS_API)) {
                Map<String, Integer> existentes = idsPorNombre(cn);
                Set<String> procesados = new HashSet<>();
                for (Pais pais : paises) {
                    String clave = normalizar(pais.getNombre());
                    if (!procesados.add(clave)) {
                        continue;
                    }
                    Integer id = existentes.get(clave);
                    if (id == null) {
                        llenarInsercion(psInsertar, pais);
                        psInsertar.addBatch();
                        registrados++;
                    } else {
                        llenarActualizacionApi(psActualizar, pais, id);
                        psActualizar.addBatch();
                        actualizados++;
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
        return new ResumenImportacion(paises.size(), registrados, actualizados);
    }

    /** Guardado de un país elegido en la consulta a la API: registra o actualiza por nombre. */
    @Override
    public Pais guardarOActualizar(Pais pais) throws SQLException {
        try (Connection cn = DBConfig.getConnection()) {
            cn.setAutoCommit(false);
            try {
                Integer id = buscarIdPorNombre(cn, pais.getNombre());
                if (id == null) {
                    id = insertar(cn, pais);
                } else {
                    try (PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR_DATOS_API)) {
                        llenarActualizacionApi(ps, pais, id);
                        ps.executeUpdate();
                    }
                }
                cn.commit();
                pais.setId(id);
                return pais;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<ConteoRegion> contarPorRegion() throws SQLException {
        // LEFT JOIN para que también aparezcan las regiones sin países (con 0).
        String sql = "SELECT r.nombre, COUNT(p.id) AS cantidad"
                + " FROM region r LEFT JOIN pais p ON p.id_region = r.id_region"
                + " GROUP BY r.id_region, r.nombre ORDER BY cantidad DESC, r.nombre";
        List<ConteoRegion> filas = new ArrayList<>();
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new ConteoRegion(rs.getString("nombre"), rs.getInt("cantidad")));
            }
        }
        return filas;
    }

    @Override
    public Pais paisMasPoblado() throws SQLException {
        List<Pais> primero = consultar(SQL_BASE + " ORDER BY p.poblacion DESC, p.nombre LIMIT 1", List.of());
        return primero.isEmpty() ? null : primero.get(0);
    }

    @Override
    public List<PromedioRegion> promedioPoblacionPorRegion() throws SQLException {
        // AVG ignora las regiones sin países (da NULL); COALESCE las deja en 0.
        String sql = "SELECT r.nombre, COALESCE(ROUND(AVG(p.poblacion)), 0) AS promedio"
                + " FROM region r LEFT JOIN pais p ON p.id_region = r.id_region"
                + " GROUP BY r.id_region, r.nombre ORDER BY promedio DESC, r.nombre";
        List<PromedioRegion> filas = new ArrayList<>();
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new PromedioRegion(rs.getString("nombre"), rs.getLong("promedio")));
            }
        }
        return filas;
    }

    // ------------------------------------------------------------------ auxiliares

    /** Ejecuta una consulta parametrizada que devuelve filas de pais. */
    private List<Pais> consultar(String sql, List<Object> parametros) throws SQLException {
        List<Pais> paises = new ArrayList<>();
        try (Connection cn = DBConfig.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paises.add(mapear(rs));
                }
            }
        }
        return paises;
    }

    /** Convierte la fila actual del ResultSet en un Pais con su región. */
    private Pais mapear(ResultSet rs) throws SQLException {
        Pais pais = new Pais(
                rs.getString("nombre"),
                rs.getString("capital"),
                new Region(rs.getInt("id_region"), rs.getString("region")),
                rs.getString("subregion"),
                rs.getLong("poblacion"),
                rs.getDouble("area"),
                rs.getString("moneda"));
        pais.setId(rs.getInt("id"));
        pais.setFechaActualizacion(rs.getObject("fecha_actualizacion", LocalDateTime.class));
        return pais;
    }

    private int insertar(Connection cn, Pais pais) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            llenarInsercion(ps, pais);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    private static void llenarInsercion(PreparedStatement ps, Pais pais) throws SQLException {
        ps.setString(1, pais.getNombre());
        ps.setString(2, pais.getCapital());
        ps.setInt(3, pais.getRegion().getId());
        ps.setString(4, pais.getSubregion());
        ps.setLong(5, pais.getPoblacion());
        ps.setDouble(6, pais.getArea());
        ps.setString(7, pais.getMoneda());
    }

    private static void llenarActualizacionApi(PreparedStatement ps, Pais pais, int id) throws SQLException {
        ps.setString(1, pais.getCapital());
        ps.setInt(2, pais.getRegion().getId());
        ps.setString(3, pais.getSubregion());
        ps.setLong(4, pais.getPoblacion());
        ps.setDouble(5, pais.getArea());
        ps.setString(6, pais.getMoneda());
        ps.setInt(7, id);
    }

    private static Integer buscarIdPorNombre(Connection cn, String nombre) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT id FROM pais WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    /** id de cada país existente, con el nombre normalizado como clave. */
    private static Map<String, Integer> idsPorNombre(Connection cn) throws SQLException {
        Map<String, Integer> ids = new HashMap<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT id, nombre FROM pais");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.put(normalizar(rs.getString("nombre")), rs.getInt("id"));
            }
        }
        return ids;
    }

    /** Patrón LIKE «contiene»: escapa los comodines % y _ que escriba el usuario. */
    private static String contiene(String texto) {
        String escapado = texto.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escapado + "%";
    }

    /** Clave de comparación sin tildes ni mayúsculas, como la intercalación ai_ci de MySQL. */
    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }
}
