package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.dto.ConteoRegion;
import ec.edu.monster.modelos.dto.PromedioRegion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a la tabla pais: CRUD, búsquedas, importación en lote y consultas de los reportes.
 *
 * @author Escobar Marcos
 */
public interface IPaisDAO {

    /** Todos los países con su región, ordenados por nombre. */
    List<Pais> listar() throws SQLException;

    /** Países cuyo nombre contiene el texto, sin distinguir mayúsculas, ordenados por nombre. */
    List<Pais> buscarPorNombre(String texto) throws SQLException;

    /**
     * Países que cumplen todos los criterios indicados (los null se ignoran): nombre y capital
     * por coincidencia parcial, región por igualdad. Ordenados por nombre.
     */
    List<Pais> buscar(String nombre, String capital, Integer idRegion) throws SQLException;

    /** País con ese id, o null si no existe. */
    Pais buscarPorId(int id) throws SQLException;

    /** True si ya existe un país con ese nombre. */
    boolean existePorNombre(String nombre) throws SQLException;

    /** Inserta el país y devuelve el id generado. */
    int insertar(Pais pais) throws SQLException;

    /** Actualiza el país por su id; devuelve false si no existe. */
    boolean actualizar(Pais pais) throws SQLException;

    /** Elimina el país; devuelve false si no existe. */
    boolean eliminar(int id) throws SQLException;

    /**
     * Registra los países nuevos y actualiza los existentes (comparando por nombre) en una sola
     * transacción con executeBatch. Si algo falla, deshace todo.
     */
    ResumenImportacion guardarTodos(List<Pais> paises) throws SQLException;

    /** Registra el país si no existe un país con ese nombre, o lo actualiza si existe. */
    Pais guardarOActualizar(Pais pais) throws SQLException;

    /** Cantidad de países de cada región, incluidas las regiones sin países, de mayor a menor. */
    List<ConteoRegion> contarPorRegion() throws SQLException;

    /** País con la mayor población (el primero por nombre si hay empate), o null si no hay países. */
    Pais paisMasPoblado() throws SQLException;

    /** Promedio de población de cada región, redondeado a entero; 0 para las regiones sin países. */
    List<PromedioRegion> promedioPoblacionPorRegion() throws SQLException;
}
