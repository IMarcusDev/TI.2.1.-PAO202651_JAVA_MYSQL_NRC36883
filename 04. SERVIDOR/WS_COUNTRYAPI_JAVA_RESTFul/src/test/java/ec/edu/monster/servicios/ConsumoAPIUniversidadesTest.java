package ec.edu.monster.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.data.Universidad;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Mapeo del JSON de la API de universidades (ECUD11) y manejo de errores.
 */
class ConsumoAPIUniversidadesTest {

    private static final String ESPE = """
            {"name":"Escuela Politécnica del Ejército","domains":["espe.edu.ec","espe.ec"],
             "web_pages":["http://www.espe.edu.ec/"],"country":"Ecuador","alpha_two_code":"EC",
             "state-province":null}""";

    private final ConsumoAPIUniversidades consumo = new ConsumoAPIUniversidades("http://no-se-usa");

    @Test
    void tomaNombrePrimerDominioPrimerSitioYPais() throws IOException {
        Universidad u = consumo.convertirRespuesta("[" + ESPE + "]", "Ecuador").get(0);
        assertEquals("Escuela Politécnica del Ejército", u.getNombre());
        assertEquals("espe.edu.ec", u.getDominio());
        assertEquals("http://www.espe.edu.ec/", u.getSitioWeb());
        assertEquals("Ecuador", u.getPais());
    }

    @Test
    void usaValoresPorDefectoSiFaltanDominioOSitio() throws IOException {
        String json = "[{\"name\":\"Universidad X\",\"domains\":[],\"web_pages\":[],\"country\":\"Ecuador\"}]";
        Universidad u = consumo.convertirRespuesta(json, "Ecuador").get(0);
        assertEquals("Sin dominio", u.getDominio());
        assertEquals("Sin sitio web", u.getSitioWeb());
    }

    @Test
    void descartaLasRepetidasConElMismoNombre() throws IOException {
        List<Universidad> lista = consumo.convertirRespuesta("[" + ESPE + "," + ESPE + "]", "Ecuador");
        assertEquals(1, lista.size());
    }

    @Test
    void listaVaciaSiLaApiNoTieneUniversidades() throws IOException {
        assertTrue(consumo.convertirRespuesta("[]", "Narnia").isEmpty());
    }

    @Test
    void unJsonNoValidoEsApiNoDisponible() {
        IOException e = assertThrows(IOException.class, () -> consumo.convertirRespuesta("{}", "Ecuador"));
        assertEquals(ConsumoAPIUniversidades.NO_DISPONIBLE, e.getMessage());
    }

    @Test
    void codificaElPaisYSigueRedirecciones() throws IOException {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.responder(200, "[" + ESPE + "]");
            servidor.redirigirUnaVez(servidor.url() + "/nuevo/search?country=Costa+Rica");
            List<Universidad> lista = new ConsumoAPIUniversidades(servidor.url()).obtenerPorPais("Costa Rica");
            assertEquals(1, lista.size());
            assertEquals("/nuevo/search?country=Costa+Rica", servidor.ultimaRuta());
        }
    }

    @Test
    void unErrorDeLaApiEsApiNoDisponible() throws IOException {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.responder(503, "caído");
            IOException e = assertThrows(IOException.class,
                    () -> new ConsumoAPIUniversidades(servidor.url()).obtenerPorPais("Ecuador"));
            assertEquals(ConsumoAPIUniversidades.NO_DISPONIBLE, e.getMessage());
        }
    }
}
