package ec.edu.monster.controladores;

import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.data.Region;
import ec.edu.monster.modelos.dto.PaisDTO;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.interfaces.IPaisDAO;
import ec.edu.monster.modelos.interfaces.IRegionDAO;
import ec.edu.monster.modelos.repositorios.PaisDAO;
import ec.edu.monster.modelos.repositorios.RegionDAO;
import ec.edu.monster.servicios.ConsumoAPI;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * Reglas de negocio de los países: CRUD (RQF-004), búsqueda y filtros (RQF-005),
 * importación (RQF-002) y consulta en la API (RQF-003).
 *
 * <p>Los errores se informan con excepciones estándar que RespuestaDTO convierte en el
 * código HTTP: IllegalArgumentException (400), NoSuchElementException (404),
 * IllegalStateException (409), IOException de la API (502) y SQLException (500).</p>
 *
 * @author Escobar Marcos
 */
public class PaisControlador {

    static final String NOMBRE_INVALIDO = "Ingrese el nombre del país";
    static final String SELECCIONE_REGION = "Seleccione una región";
    static final String REGION_NO_VALIDA = "Región no válida";
    static final String POBLACION_INVALIDA = "La población debe ser un número entero mayor o igual que cero";
    static final String AREA_INVALIDA = "El área debe ser un número mayor o igual que cero";
    static final String TEXTOS_LARGOS = "La capital, la subregión y la moneda deben tener como máximo 100 caracteres";
    static final String NOMBRE_REPETIDO = "Ya existe un país con ese nombre";
    static final String PAIS_NO_EXISTE = "El país no existe";
    static final String NOMBRE_CORTO = "Ingrese al menos dos caracteres";

    private static final int LARGO_MAXIMO = 100;
    private static final Logger LOG = Logger.getLogger(PaisControlador.class.getName());

    private final IPaisDAO paisDAO;
    private final IRegionDAO regionDAO;
    private final ConsumoAPI consumoAPI;

    public PaisControlador() {
        this(new PaisDAO(), new RegionDAO(), new ConsumoAPI());
    }

    public PaisControlador(IPaisDAO paisDAO, IRegionDAO regionDAO, ConsumoAPI consumoAPI) {
        this.paisDAO = paisDAO;
        this.regionDAO = regionDAO;
        this.consumoAPI = consumoAPI;
    }

    /** Países almacenados, ordenados por nombre. */
    public List<Pais> listarPaises() throws SQLException {
        return paisDAO.listar();
    }

    /** Países cuyo nombre contiene el texto; sin texto, el listado completo. */
    public List<Pais> buscarPorNombre(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return paisDAO.listar();
        }
        return paisDAO.buscarPorNombre(texto.trim());
    }

    /** Registra un país nuevo (HTTP 201). El nombre no puede repetirse. */
    public Pais registrarPais(Pais pais) throws SQLException {
        validar(pais);
        if (paisDAO.existePorNombre(pais.getNombre())) {
            throw new IllegalStateException(NOMBRE_REPETIDO);
        }
        try {
            pais.setId(paisDAO.insertar(pais));
        } catch (SQLIntegrityConstraintViolationException e) {
            // Otro usuario registró el mismo nombre entre la verificación y el INSERT.
            throw new IllegalStateException(NOMBRE_REPETIDO);
        }
        return pais;
    }

    /** Actualiza los siete datos del país. El índice UNIQUE de nombre detecta los repetidos. */
    public Pais actualizarPais(int id, Pais pais) throws SQLException {
        validar(pais);
        pais.setId(id);
        boolean actualizado;
        try {
            actualizado = paisDAO.actualizar(pais);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException(NOMBRE_REPETIDO);
        }
        if (!actualizado) {
            throw new NoSuchElementException(PAIS_NO_EXISTE);
        }
        Pais guardado = paisDAO.buscarPorId(id);
        return guardado == null ? pais : guardado;
    }

    /** Elimina el país; 404 si no existe. */
    public void eliminarPais(int id) throws SQLException {
        if (!paisDAO.eliminar(id)) {
            throw new NoSuchElementException(PAIS_NO_EXISTE);
        }
    }

    /**
     * Búsqueda combinable: nombre y capital por coincidencia parcial sin distinguir mayúsculas,
     * región por igualdad. Sin criterios devuelve todos los países.
     */
    public List<Pais> buscarPaises(String nombre, String capital, String region) throws SQLException {
        Integer idRegion = null;
        if (region != null && !region.isBlank()) {
            Region encontrada = regionDAO.buscarPorNombre(region.trim());
            if (encontrada == null) {
                throw new IllegalArgumentException(REGION_NO_VALIDA);
            }
            idRegion = encontrada.getId();
        }
        return paisDAO.buscar(vacioANulo(nombre), vacioANulo(capital), idRegion);
    }

    /**
     * Importa todos los países de REST Countries (ECUD2). Si la API falla, se lanza la IOException
     * antes de tocar la base. Los países se guardan en una sola transacción.
     */
    public ResumenImportacion importarPaises() throws IOException, SQLException {
        List<Pais> recibidos = consumoAPI.obtenerTodos();

        // Cada región traducida por ConsumoAPI se reemplaza por la del catálogo, con su id.
        Map<String, Region> regiones = new HashMap<>();
        for (Region r : regionDAO.listar()) {
            regiones.put(r.getNombre(), r);
        }
        List<Pais> paraGuardar = new ArrayList<>(recibidos.size());
        for (Pais pais : recibidos) {
            Region region = regiones.get(pais.getNombreRegion());
            if (region == null) {
                LOG.warning(() -> "País descartado, región fuera del catálogo: " + pais.getNombre());
                continue;
            }
            pais.setRegion(region);
            paraGuardar.add(pais);
        }

        ResumenImportacion resumen = paisDAO.guardarTodos(paraGuardar);
        resumen.setRecibidos(recibidos.size());
        return resumen;
    }

    /**
     * Consulta los países que coinciden con el nombre en la API (ECUD3) y marca los que ya
     * están guardados.
     */
    public List<PaisDTO> consultarEnAPI(String nombre) throws IOException, SQLException {
        if (nombre == null || nombre.trim().length() < 2) {
            throw new IllegalArgumentException(NOMBRE_CORTO);
        }
        List<PaisDTO> resultado = new ArrayList<>();
        for (Pais pais : consumoAPI.buscarPorNombre(nombre)) {
            resultado.add(PaisDTO.desde(pais, paisDAO.existePorNombre(pais.getNombre())));
        }
        return resultado;
    }

    /** Guarda el país elegido de la consulta a la API: lo registra o lo actualiza por nombre. */
    public Pais guardarDesdeAPI(Pais pais) throws SQLException {
        validar(pais);
        return paisDAO.guardarOActualizar(pais);
    }

    /**
     * Repite en el servidor las validaciones del cliente (ECUD4, flujo alternativo 2), limpia
     * los espacios y completa la región con su id del catálogo.
     */
    private void validar(Pais pais) throws SQLException {
        if (pais == null || pais.getNombre() == null || pais.getNombre().isBlank()
                || pais.getNombre().trim().length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException(NOMBRE_INVALIDO);
        }
        String nombreRegion = pais.getNombreRegion();
        if (nombreRegion == null || nombreRegion.isBlank()) {
            throw new IllegalArgumentException(SELECCIONE_REGION);
        }
        if (pais.getPoblacion() < 0) {
            throw new IllegalArgumentException(POBLACION_INVALIDA);
        }
        if (pais.getArea() < 0 || Double.isNaN(pais.getArea()) || Double.isInfinite(pais.getArea())) {
            throw new IllegalArgumentException(AREA_INVALIDA);
        }
        pais.setNombre(pais.getNombre().trim());
        pais.setCapital(vacioANulo(pais.getCapital()));
        pais.setSubregion(vacioANulo(pais.getSubregion()));
        pais.setMoneda(vacioANulo(pais.getMoneda()));
        if (muyLargo(pais.getCapital()) || muyLargo(pais.getSubregion()) || muyLargo(pais.getMoneda())) {
            throw new IllegalArgumentException(TEXTOS_LARGOS);
        }
        Region region = regionDAO.buscarPorNombre(nombreRegion.trim());
        if (region == null) {
            throw new IllegalArgumentException(REGION_NO_VALIDA);
        }
        pais.setRegion(region);
    }

    private static boolean muyLargo(String texto) {
        return texto != null && texto.length() > LARGO_MAXIMO;
    }

    private static String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
