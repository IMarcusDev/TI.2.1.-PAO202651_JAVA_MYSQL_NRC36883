package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.config.ConfigServidor;
import ec.edu.monster.dobles.ServidorFalso;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.servicios.PaisServicio;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Lo propio del cliente móvil: la dirección del servidor que el usuario escribe en la app y el
 * cambio de servidor sin reiniciar.
 */
class ConfigServidorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "http://10.0.2.2:8080/WS_COUNTRYAPI_JAVA_RESTFul/api",
        "http://192.168.1.20:8080/WS_COUNTRYAPI_JAVA_RESTFul/api/",
        "https://servidor.espe.edu.ec/api",
        "  http://localhost:8080/api  "})
    void aceptaDireccionesHttpConEquipo(String url) {
        assertTrue(ConfigServidor.esUrlValida(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "192.168.1.20:8080/api", "ftp://192.168.1.20/api", "http://", "http:// /api",
        "localhost"})
    void rechazaDireccionesSinHttpOSinEquipo(String url) {
        assertFalse(ConfigServidor.esUrlValida(url));
    }

    @Test
    void rechazaNull() {
        assertFalse(ConfigServidor.esUrlValida(null));
    }

    @Test
    void quitaLasBarrasDelFinal() {
        assertEquals("http://10.0.2.2:8080/api", ConfigServidor.quitarBarraFinal("http://10.0.2.2:8080/api//"));
        assertEquals("http://10.0.2.2:8080/api", ConfigServidor.quitarBarraFinal("http://10.0.2.2:8080/api"));
    }

    @Test
    void laDireccionPorDefectoEsLaDelEmuladorSinBarraFinal() {
        assertTrue(ConfigServidor.URL_POR_DEFECTO.endsWith("/WS_COUNTRYAPI_JAVA_RESTFul/api"));
        assertEquals(15000, ConfigServidor.TIMEOUT_MS);
    }

    @Test
    void cambiarElServidorAfectaALasPeticionesSiguientes() throws IOException {
        try (ServidorFalso primero = new ServidorFalso(); ServidorFalso segundo = new ServidorFalso()) {
            primero.ok("GET /Paises", "Consulta realizada correctamente", "[]");
            segundo.ok("GET /Paises", "Consulta realizada correctamente",
                    "[{\"id\":1,\"nombre\":\"Ecuador\",\"region\":\"América\",\"poblacion\":17643060}]");
            ClienteRest cliente = new ClienteRest(primero.urlBase() + "/");
            PaisServicio servicio = new PaisServicio(cliente);
            assertEquals(primero.urlBase(), cliente.getUrlBase());

            RespuestaDTO<List<Pais>> antes = servicio.listar("t", null);
            cliente.setUrlBase(segundo.urlBase());
            RespuestaDTO<List<Pais>> despues = servicio.listar("t", null);

            assertTrue(antes.getDatos().isEmpty());
            assertEquals("Ecuador", despues.getDatos().get(0).getNombre());
            assertEquals(1, primero.peticiones().size());
            assertEquals(1, segundo.peticiones().size());
        }
    }

    @Test
    void codificarFuncionaSinLasApisDeJava10() {
        assertEquals("Costa%20Rica", ClienteRest.codificar("Costa Rica"));
        assertEquals("Per%C3%BA", ClienteRest.codificar("Perú"));
        assertEquals("a%26b%3Dc", ClienteRest.codificar("a&b=c"));
    }
}
