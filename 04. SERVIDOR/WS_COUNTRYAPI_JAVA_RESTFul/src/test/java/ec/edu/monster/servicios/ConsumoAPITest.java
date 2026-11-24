package ec.edu.monster.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.data.Pais;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Mapeo del JSON de REST Countries v5 a los siete datos del taller y manejo de errores (ECUD8).
 */
class ConsumoAPITest {

    /** Ecuador tal como lo devuelve REST Countries v5 con los campos que pide el sistema. */
    private static final String ECUADOR = """
            {"names":{"common":"Ecuador","translations":{"spa":{"common":"Ecuador","official":"República del Ecuador"}}},
             "capitals":[{"attributes":{"primary":true},"coordinates":{"lat":-0.22,"lng":-78.5},"name":"Quito"}],
             "region":"Americas","subregion":"South America",
             "area":{"kilometers":276841,"miles":106888.9},
             "currencies":[{"code":"USD","name":"United States dollar","symbol":"$"}],
             "population":18135478}""";

    private final ConsumoAPI consumoAPI = new ConsumoAPI("http://no-se-usa", "clave-de-prueba");

    /** Envuelve los países como una página de la v5. */
    private static String pagina(boolean hayMas, String... paises) {
        return "{\"data\":{\"objects\":[" + String.join(",", paises) + "],\"meta\":{\"count\":"
                + paises.length + ",\"more\":" + hayMas + "}}}";
    }

    @Test
    void convierteLosSieteDatosDeUnPaisCompleto() throws IOException {
        Pais pais = consumoAPI.convertirRespuesta(pagina(false, ECUADOR)).get(0);
        assertEquals("Ecuador", pais.getNombre());
        assertEquals("Quito", pais.getCapital());
        assertEquals("América", pais.getNombreRegion());
        assertEquals("South America", pais.getSubregion());
        assertEquals(18135478L, pais.getPoblacion());
        assertEquals(276841.0, pais.getArea());
        assertEquals("United States dollar (USD)", pais.getMoneda());
    }

    @Test
    void usaElNombreEnEspanolCuandoExiste() throws IOException {
        String alemania = """
                {"names":{"common":"Germany","translations":{"spa":{"common":"Alemania"}}},
                 "capitals":[{"name":"Berlin"}],"region":"Europe","subregion":"Western Europe",
                 "population":83240525,"area":{"kilometers":357114},"currencies":[{"code":"EUR","name":"Euro"}]}""";
        Pais pais = consumoAPI.convertirRespuesta(pagina(false, alemania)).get(0);
        assertEquals("Alemania", pais.getNombre());
        assertEquals("Berlin", pais.getCapital(), "sin primary, se toma la primera capital");
        assertEquals("Europa", pais.getNombreRegion());
        assertEquals("Euro (EUR)", pais.getMoneda());
    }

    @Test
    void usaElNombreComunSiNoHayTraduccionAlEspanol() throws IOException {
        String kosovo = """
                {"names":{"common":"Kosovo","translations":{}},"capitals":[{"name":"Pristina"}],
                 "region":"Europe","subregion":"Southeast Europe","population":1775378,
                 "area":{"kilometers":10908},"currencies":[{"code":"EUR","name":"Euro"}]}""";
        assertEquals("Kosovo", consumoAPI.convertirRespuesta(pagina(false, kosovo)).get(0).getNombre());
    }

    @Test
    void tomaLaCapitalMarcadaComoPrincipal() throws IOException {
        String sudafrica = """
                {"names":{"common":"South Africa","translations":{"spa":{"common":"Sudáfrica"}}},
                 "capitals":[{"name":"Bloemfontein","attributes":{"primary":false,"judicial":true}},
                             {"name":"Pretoria","attributes":{"primary":true}},
                             {"name":"Cape Town","attributes":{"legislative":true}}],
                 "region":"Africa","subregion":"Southern Africa","population":1,"area":{"kilometers":1},
                 "currencies":[{"code":"ZAR","name":"South African rand"}]}""";
        Pais pais = consumoAPI.convertirRespuesta(pagina(false, sudafrica)).get(0);
        assertEquals("Pretoria", pais.getCapital());
        assertEquals("África", pais.getNombreRegion());
    }

    @Test
    void aplicaLosValoresPorDefectoCuandoFaltanDatos() throws IOException {
        // Isla Bouvet: sin capital, sin subregión, sin moneda y sin área.
        String bouvet = """
                {"names":{"common":"Bouvet Island","translations":{"spa":{"common":"Isla Bouvet"}}},
                 "capitals":[],"region":"Antarctic","subregion":"","population":0,"currencies":[]}""";
        Pais pais = consumoAPI.convertirRespuesta(pagina(false, bouvet)).get(0);
        assertEquals("Isla Bouvet", pais.getNombre());
        assertEquals("Sin capital", pais.getCapital());
        assertEquals("Antártida", pais.getNombreRegion());
        assertEquals("Sin subregión", pais.getSubregion());
        assertEquals(0.0, pais.getArea());
        assertEquals("Sin moneda", pais.getMoneda());
    }

    @Test
    void traduceLasSeisRegiones() throws IOException {
        String[][] casos = {
            {"Americas", "América"}, {"Europe", "Europa"}, {"Asia", "Asia"},
            {"Africa", "África"}, {"Oceania", "Oceanía"}, {"Antarctic", "Antártida"}};
        for (String[] caso : casos) {
            String json = pagina(false, "{\"names\":{\"common\":\"X\"},\"region\":\"" + caso[0] + "\",\"population\":1}");
            assertEquals(caso[1], consumoAPI.convertirRespuesta(json).get(0).getNombreRegion());
        }
    }

    @Test
    void descartaLosPaisesConRegionDesconocida() throws IOException {
        String atlantis = "{\"names\":{\"common\":\"Atlantis\"},\"region\":\"Ocean\",\"population\":5}";
        List<Pais> paises = consumoAPI.convertirRespuesta(pagina(false, ECUADOR, atlantis));
        assertEquals(1, paises.size());
        assertEquals("Ecuador", paises.get(0).getNombre());
    }

    @Test
    void rechazaUnJsonSinDataObjects() {
        IOException e = assertThrows(IOException.class,
                () -> consumoAPI.convertirRespuesta("{\"errors\":[{\"message\":\"Not found\"}]}"));
        assertEquals(ConsumoAPI.RESPUESTA_NO_VALIDA, e.getMessage());
        assertThrows(IOException.class, () -> consumoAPI.convertirRespuesta("[" + ECUADOR + "]"));
        assertThrows(IOException.class, () -> consumoAPI.convertirRespuesta("<html>error</html>"));
        assertThrows(IOException.class, () -> consumoAPI.convertirRespuesta(""));
    }

    @Test
    void obtenerTodosRecorreLasPaginasYEnviaLaClave() throws IOException {
        try (ServidorFalso servidor = new ServidorFalso()) {
            // Página 1 (offset=0) con más páginas; página 2 (offset=1) es la última.
            servidor.responderSegun(ruta -> ruta.contains("offset=0") ? pagina(true, ECUADOR)
                    : pagina(false, ECUADOR.replace("\"common\":\"Ecuador\",\"translations\"",
                            "\"common\":\"Chile\",\"translations\"").replace("República del Ecuador", "Chile")
                            .replace("\"spa\":{\"common\":\"Ecuador\"", "\"spa\":{\"common\":\"Chile\"")));
            List<Pais> paises = new ConsumoAPI(servidor.url(), "rc_prueba").obtenerTodos();
            assertEquals(2, paises.size());
            assertEquals("Chile", paises.get(1).getNombre());
            assertEquals(2, servidor.rutas().size());
            assertTrue(servidor.rutas().get(0).startsWith("/?limit=100&offset=0&response_fields="), servidor.rutas().get(0));
            assertTrue(servidor.rutas().get(1).startsWith("/?limit=100&offset=1&"), servidor.rutas().get(1));
            assertEquals("Bearer rc_prueba", servidor.ultimoAuthorization());
        }
    }

    @Test
    void buscarPorNombreUsaElAgregadoNameYCodificaElTexto() throws IOException {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.responder(200, pagina(false, ECUADOR));
            new ConsumoAPI(servidor.url(), "rc_prueba").buscarPorNombre("costa rica");
            assertTrue(servidor.ultimaRuta().startsWith("/name?q=costa+rica&limit=100&response_fields="),
                    servidor.ultimaRuta());
        }
    }

    @Test
    void buscarPorNombreSinCoincidenciasEsNoEncontrado() throws IOException {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.responder(200, pagina(false));
            NoSuchElementException e = assertThrows(NoSuchElementException.class,
                    () -> new ConsumoAPI(servidor.url(), "rc_prueba").buscarPorNombre("zzzz"));
            assertEquals(ConsumoAPI.SIN_COINCIDENCIAS, e.getMessage());
        }
    }

    @Test
    void unaClaveRechazadaEsApiNoDisponible() throws IOException {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.responder(401, "{\"errors\":[{\"message\":\"Authorization key required.\"}]}");
            IOException e = assertThrows(IOException.class,
                    () -> new ConsumoAPI(servidor.url(), "rc_mala").obtenerTodos());
            assertEquals(ConsumoAPI.NO_DISPONIBLE, e.getMessage());
        }
    }

    @Test
    void sinClaveNoSeLlamaALaApi() throws IOException {
        try (ServidorFalso servidor = new ServidorFalso()) {
            IOException e = assertThrows(IOException.class, () -> new ConsumoAPI(servidor.url(), " ").obtenerTodos());
            assertEquals(ConsumoAPI.NO_DISPONIBLE, e.getMessage());
            assertTrue(servidor.rutas().isEmpty());
        }
    }

    @Test
    void sinConexionEsApiNoDisponible() {
        // Puerto 9 (discard): no hay nada escuchando, la conexión se rechaza.
        IOException e = assertThrows(IOException.class,
                () -> new ConsumoAPI("http://127.0.0.1:9", "rc_prueba").obtenerTodos());
        assertEquals(ConsumoAPI.NO_DISPONIBLE, e.getMessage());
    }
}
