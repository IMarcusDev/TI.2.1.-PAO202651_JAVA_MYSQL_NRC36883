package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dobles.ServidorFalso;
import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.LoginResponse;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.Reporte;
import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.servicios.LoginServicio;
import ec.edu.monster.servicios.PaisServicio;
import ec.edu.monster.servicios.ReporteServicio;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Servicios y ClienteRest contra un servidor falso: rutas, encabezados, cuerpo JSON y
 * manejo de errores (401, sin conexión, respuesta no válida).
 */
class ServiciosTest {

    private ServidorFalso servidor;
    private ClienteRest cliente;

    @BeforeEach
    void iniciar() throws IOException {
        servidor = new ServidorFalso();
        cliente = new ClienteRest(servidor.urlBase(), 5000);
    }

    @AfterEach
    void detener() {
        servidor.close();
    }

    @Test
    void loginEnviaLasCredencialesYLeeElToken() {
        servidor.ok("POST /Login/login", "Ingreso de sesión exitoso", "{\"token\":\"t-1\",\"nombre\":\"Marcos Escobar\"}");
        RespuestaDTO<LoginResponse> r = new LoginServicio(cliente).login("monster", "monster9");
        assertTrue(r.esOk());
        assertEquals("t-1", r.getDatos().getToken());
        assertEquals("Marcos Escobar", r.getDatos().getNombre());
        assertEquals("{\"usuario\":\"monster\",\"clave\":\"monster9\"}", servidor.ultima().cuerpo());
        assertNull(servidor.ultima().authorization(), "el login no lleva token");
    }

    @Test
    void credencialesIncorrectasConservanElMensajeDelServidor() {
        servidor.error("POST /Login/login", 401, "Credenciales incorrectas");
        RespuestaDTO<LoginResponse> r = new LoginServicio(cliente).login("monster", "x");
        assertFalse(r.esOk());
        assertEquals("Credenciales incorrectas", r.getMensaje());
        assertFalse(r.isSesionExpirada(), "sin token, un 401 no es una sesión expirada");
    }

    @Test
    void un401ConTokenEsSesionExpirada() {
        servidor.error("GET /Paises", 401, "Token inválido o sesión expirada");
        RespuestaDTO<List<Pais>> r = new PaisServicio(cliente).listar("t-1", null);
        assertTrue(r.isSesionExpirada());
        assertEquals(ClienteRest.SESION_EXPIRADA, r.getMensaje());
        assertEquals("Bearer t-1", servidor.ultima().authorization());
    }

    @Test
    void listarConviertePaisesConTildes() {
        servidor.ok("GET /Paises", "Consulta realizada correctamente",
                "[{\"id\":3,\"nombre\":\"Perú\",\"capital\":\"Lima\",\"region\":\"América\",\"subregion\":\"South America\","
                + "\"poblacion\":32971846,\"area\":1285216.0,\"moneda\":\"Peruvian sol (PEN)\",\"guardado\":true}]");
        RespuestaDTO<List<Pais>> r = new PaisServicio(cliente).listar("t-1", "per ú");
        Pais peru = r.getDatos().get(0);
        assertEquals("Perú", peru.getNombre());
        assertEquals(32971846L, peru.getPoblacion());
        assertEquals("/Paises?nombre=per%20%C3%BA", servidor.ultima().ruta());
    }

    @Test
    void buscarEnviaSoloLosCriteriosConValor() {
        servidor.ok("GET /Paises/buscar", "Búsqueda realizada correctamente", "[]");
        new PaisServicio(cliente).buscar("t-1", new CriteriosBusqueda(" ", "quito", "América"));
        assertEquals("/Paises/buscar?capital=quito&region=Am%C3%A9rica", servidor.ultima().ruta());
        new PaisServicio(cliente).buscar("t-1", new CriteriosBusqueda(null, null, null));
        assertEquals("/Paises/buscar", servidor.ultima().ruta());
    }

    @Test
    void registrarEnviaElPaisComoJsonEnUtf8() {
        servidor.responder("POST /Paises", 201,
                "{\"estado\":\"OK\",\"mensaje\":\"País registrado correctamente\",\"datos\":{\"id\":20,\"nombre\":\"Japón\"}}");
        RespuestaDTO<Pais> r = new PaisServicio(cliente).registrar("t-1",
                new Pais("Japón", "Tokyo", "Asia", "Eastern Asia", 1, 2.5, "Yen"));
        assertEquals(20, r.getDatos().getId());
        assertEquals(201, r.getCodigoHttp());
        assertTrue(servidor.ultima().cuerpo().contains("\"nombre\":\"Japón\""), servidor.ultima().cuerpo());
    }

    @Test
    void eliminarYConsultarExternoUsanLaRutaCorrecta() {
        servidor.ok("DELETE /Paises/7", "País eliminado correctamente", "null");
        assertTrue(new PaisServicio(cliente).eliminar("t-1", 7).esOk());
        servidor.ok("GET /Paises/externo/costa rica", "Consulta en la API exitosa", "[]");
        new PaisServicio(cliente).consultarExterno("t-1", "costa rica");
        assertEquals("/Paises/externo/costa%20rica", servidor.ultima().ruta());
    }

    @Test
    void reporteConvierteLosTresReportes() {
        servidor.ok("GET /Reportes", "Reportes generados correctamente",
                "{\"paisesPorRegion\":[{\"region\":\"América\",\"cantidad\":4}],"
                + "\"paisMasPoblado\":{\"nombre\":\"China\",\"poblacion\":1402112000},"
                + "\"promedioPorRegion\":[{\"region\":\"Asia\",\"promedioPoblacion\":969317469}]}");
        Reporte reporte = new ReporteServicio(cliente).generar("t-1").getDatos();
        assertEquals(4, reporte.getPaisesPorRegion().get(0).getCantidad());
        assertEquals("China", reporte.getPaisMasPoblado().getNombre());
        assertEquals(969317469L, reporte.getPromedioPorRegion().get(0).getPromedioPoblacion());
    }

    @Test
    void sinConexionDaElMensajeDelEcud() {
        ClienteRest sinServidor = new ClienteRest("http://127.0.0.1:9/api", 2000);
        RespuestaDTO<List<Pais>> r = new PaisServicio(sinServidor).listar("t-1", null);
        assertFalse(r.esOk());
        assertEquals("No se pudo conectar con el servidor. Verifique la red e intente nuevamente.", r.getMensaje());
        assertFalse(sinServidor.servidorDisponible());
    }

    @Test
    void unaRespuestaQueNoEsJsonEsUnErrorInesperado() {
        servidor.responder("GET /Reportes", 500, "<html>Error</html>");
        RespuestaDTO<Reporte> r = new ReporteServicio(cliente).generar("t-1");
        assertFalse(r.esOk());
        assertEquals(ClienteRest.RESPUESTA_NO_VALIDA, r.getMensaje());
    }

    @Test
    void elServidorFalsoSeReportaDisponible() {
        assertTrue(cliente.servidorDisponible());
    }
}
