package ec.edu.monster.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.data.Region;
import ec.edu.monster.modelos.dto.PaisDTO;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.servicios.ConsumoAPI;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Reglas de negocio de PaisControlador con DAO en memoria y una API falsa.
 */
class PaisControladorTest {

    private DaosFalsos.PaisDAOFalso paisDAO;
    private PaisControlador controlador;
    /** Lo que devolverá la API falsa. */
    private List<Pais> respuestaApi;

    @BeforeEach
    void preparar() {
        paisDAO = new DaosFalsos.PaisDAOFalso();
        paisDAO.agregar("Ecuador", "América", 17_643_060);
        ConsumoAPI apiFalsa = new ConsumoAPI() {
            @Override
            public List<Pais> obtenerTodos() {
                return respuestaApi;
            }

            @Override
            public List<Pais> buscarPorNombre(String nombre) {
                return respuestaApi;
            }
        };
        controlador = new PaisControlador(paisDAO, new DaosFalsos.RegionDAOFalso(), apiFalsa);
    }

    private static Pais pais(String nombre, String region, long poblacion, double area) {
        return new Pais(nombre, "Capital", region == null ? null : new Region(0, region), "Sub", poblacion, area, "Moneda");
    }

    private static void assertMensaje(Class<? extends Throwable> tipo, String mensaje, Runnable accion) {
        Throwable e = assertThrows(tipo, accion::run);
        assertEquals(mensaje, e.getMessage());
    }

    /** Ejecuta una operación que declara SQLException dentro de assertThrows. */
    private interface Operacion {
        void ejecutar() throws Exception;
    }

    private static Runnable r(Operacion op) {
        return () -> {
            try {
                op.ejecutar();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        };
    }

    @Test
    void registraUnPaisValidoConLaRegionDelCatalogo() throws SQLException {
        Pais registrado = controlador.registrarPais(pais("  Perú  ", "América", 33_000_000, 1_285_216));
        assertTrue(registrado.getId() > 0);
        assertEquals("Perú", registrado.getNombre());
        assertEquals(1, registrado.getRegion().getId());
    }

    @Test
    void validaElNombre() {
        assertMensaje(IllegalArgumentException.class, PaisControlador.NOMBRE_INVALIDO,
                r(() -> controlador.registrarPais(pais("  ", "América", 1, 1))));
        assertMensaje(IllegalArgumentException.class, PaisControlador.NOMBRE_INVALIDO,
                r(() -> controlador.registrarPais(pais("x".repeat(101), "América", 1, 1))));
        assertMensaje(IllegalArgumentException.class, PaisControlador.NOMBRE_INVALIDO,
                r(() -> controlador.registrarPais(null)));
    }

    @Test
    void validaRegionPoblacionAreaYLargos() {
        assertMensaje(IllegalArgumentException.class, PaisControlador.SELECCIONE_REGION,
                r(() -> controlador.registrarPais(pais("Chile", null, 1, 1))));
        assertMensaje(IllegalArgumentException.class, PaisControlador.REGION_NO_VALIDA,
                r(() -> controlador.registrarPais(pais("Chile", "Atlántida", 1, 1))));
        assertMensaje(IllegalArgumentException.class, PaisControlador.POBLACION_INVALIDA,
                r(() -> controlador.registrarPais(pais("Chile", "América", -1, 1))));
        assertMensaje(IllegalArgumentException.class, PaisControlador.AREA_INVALIDA,
                r(() -> controlador.registrarPais(pais("Chile", "América", 1, -0.5))));
        Pais conCapitalLarga = pais("Chile", "América", 1, 1);
        conCapitalLarga.setCapital("c".repeat(101));
        assertMensaje(IllegalArgumentException.class, PaisControlador.TEXTOS_LARGOS,
                r(() -> controlador.registrarPais(conCapitalLarga)));
    }

    @Test
    void aceptaPoblacionYAreaCero() throws SQLException {
        assertTrue(controlador.registrarPais(pais("Isla", "Antártida", 0, 0)).getId() > 0);
    }

    @Test
    void noPermiteRegistrarUnNombreRepetido() {
        assertMensaje(IllegalStateException.class, PaisControlador.NOMBRE_REPETIDO,
                r(() -> controlador.registrarPais(pais("ecuador", "América", 1, 1))));
    }

    @Test
    void actualizarUnPaisInexistenteEsNoEncontrado() {
        assertMensaje(NoSuchElementException.class, PaisControlador.PAIS_NO_EXISTE,
                r(() -> controlador.actualizarPais(999, pais("Chile", "América", 1, 1))));
    }

    @Test
    void actualizarConElNombreDeOtroPaisEsConflicto() throws SQLException {
        Pais colombia = paisDAO.agregar("Colombia", "América", 50_000_000);
        assertMensaje(IllegalStateException.class, PaisControlador.NOMBRE_REPETIDO,
                r(() -> controlador.actualizarPais(colombia.getId(), pais("Ecuador", "América", 1, 1))));
    }

    @Test
    void actualizarConservandoSuPropioNombreFunciona() throws SQLException {
        Pais actualizado = controlador.actualizarPais(1, pais("Ecuador", "América", 18_000_000, 276_841));
        assertEquals(18_000_000, actualizado.getPoblacion());
    }

    @Test
    void eliminarUnPaisInexistenteEsNoEncontrado() throws SQLException {
        controlador.eliminarPais(1);
        assertMensaje(NoSuchElementException.class, PaisControlador.PAIS_NO_EXISTE,
                r(() -> controlador.eliminarPais(1)));
    }

    @Test
    void buscarConUnaRegionInexistenteEsRegionNoValida() {
        assertMensaje(IllegalArgumentException.class, PaisControlador.REGION_NO_VALIDA,
                r(() -> controlador.buscarPaises(null, null, "Narnia")));
    }

    @Test
    void buscarFiltraPorRegionYSinCriteriosDevuelveTodo() throws SQLException {
        paisDAO.agregar("España", "Europa", 47_000_000);
        // La región del país agregado se resuelve igual que en la base: por id del catálogo.
        paisDAO.paises.values().forEach(p -> p.setRegion(new DaosFalsos.RegionDAOFalso().buscarPorNombre(p.getNombreRegion())));
        assertEquals(1, controlador.buscarPaises(null, null, "Europa").size());
        assertEquals(2, controlador.buscarPaises("", " ", "").size());
    }

    @Test
    void consultaEnLaApiExigeDosCaracteres() {
        assertMensaje(IllegalArgumentException.class, PaisControlador.NOMBRE_CORTO,
                r(() -> controlador.consultarEnAPI(" e ")));
        assertMensaje(IllegalArgumentException.class, PaisControlador.NOMBRE_CORTO,
                r(() -> controlador.consultarEnAPI(null)));
    }

    @Test
    void consultaEnLaApiMarcaLosPaisesYaGuardados() throws IOException, SQLException {
        respuestaApi = List.of(pais("Ecuador", "América", 1, 1), pais("Chile", "América", 1, 1));
        List<PaisDTO> resultado = controlador.consultarEnAPI("ec");
        assertTrue(resultado.get(0).isGuardado());
        assertFalse(resultado.get(1).isGuardado());
    }

    @Test
    void importarAsignaLaRegionDelCatalogoYCuentaLosRecibidos() throws IOException, SQLException {
        respuestaApi = List.of(
                pais("Ecuador", "América", 1, 1),
                pais("Chile", "América", 1, 1),
                pais("Japón", "Asia", 1, 1));
        ResumenImportacion resumen = controlador.importarPaises();
        assertEquals(3, resumen.getRecibidos());
        assertEquals(2, resumen.getRegistrados());
        assertEquals(1, resumen.getActualizados());
        assertEquals(3, paisDAO.ultimoLoteImportado.get(2).getRegion().getId());
    }

    @Test
    void siLaApiFallaNoSeTocaLaBase() {
        ConsumoAPI apiCaida = new ConsumoAPI() {
            @Override
            public List<Pais> obtenerTodos() throws IOException {
                throw new IOException("La API REST Countries no está disponible, intente más tarde");
            }
        };
        PaisControlador conApiCaida = new PaisControlador(paisDAO, new DaosFalsos.RegionDAOFalso(), apiCaida);
        assertThrows(IOException.class, conApiCaida::importarPaises);
        assertNull(paisDAO.ultimoLoteImportado);
    }
}
