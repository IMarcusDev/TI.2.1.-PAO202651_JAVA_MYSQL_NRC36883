package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.App;
import ec.edu.monster.dobles.ServidorFalso;
import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.vista.BusquedaConsola;
import ec.edu.monster.vista.Consola;
import ec.edu.monster.vista.LoginConsola;
import ec.edu.monster.vista.PaisConsola;
import ec.edu.monster.vista.Tabla;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Lo propio del cliente de consola: tablas de texto, lectura de datos campo por campo y el
 * ciclo completo inicio de sesión → menú → cierre de sesión contra un servidor falso.
 */
class ConsolaTest {

    private static final String PERU = "{\"id\":3,\"nombre\":\"Perú\",\"capital\":\"Lima\",\"region\":\"América\","
            + "\"subregion\":\"South America\",\"poblacion\":32971846,\"area\":1285216.0,"
            + "\"moneda\":\"Peruvian sol (PEN)\"}";

    /** Consola que lee las líneas indicadas y guarda todo lo que se muestra. */
    private static final class Guion {
        final ByteArrayOutputStream capturado = new ByteArrayOutputStream();
        final Consola consola;

        Guion(String... lineas) {
            String entrada = lineas.length == 0 ? "" : String.join("\n", lineas) + "\n";
            consola = new Consola(new BufferedReader(new StringReader(entrada)),
                    new PrintStream(capturado, true, StandardCharsets.UTF_8));
        }

        String salida() {
            return capturado.toString(StandardCharsets.UTF_8);
        }
    }

    private static SesionServicio sesionActiva() {
        SesionServicio sesion = new SesionServicio();
        sesion.guardar("t-1", "Marcos Escobar");
        return sesion;
    }

    @Nested
    class Tablas {

        @Test
        void alineaNumerosALaDerechaYTextosALaIzquierda() {
            String texto = new Tabla("Región", "Países").derecha(1).fila("Asia", "52").fila("Antártida", "5").texto();
            List<String> lineas = texto.lines().toList();
            assertEquals("+-----------+--------+", lineas.get(0));
            assertEquals("| Región    | Países |", lineas.get(1));
            assertEquals("| Asia      |     52 |", lineas.get(3));
            assertEquals("| Antártida |      5 |", lineas.get(4));
            assertEquals(lineas.get(0), lineas.get(5));
        }

        @Test
        void cortaLosTextosLargosConPuntosSuspensivos() {
            String texto = new Tabla("Moneda").maximo(0, 8).fila("United States dollar (USD)").texto();
            assertTrue(texto.contains("| United … |"), texto);
        }

        @Test
        void sinFilasMuestraSoloElEncabezado() {
            assertEquals(3, new Tabla("A", "B").texto().lines().count());
        }

        @Test
        void formateaNumerosConSeparadorDeMiles() {
            assertEquals("17.643.060", Tabla.entero(17_643_060));
            assertEquals("276.841,5", Tabla.decimal(276_841.5));
        }
    }

    @Nested
    class Pantallas {

        @Test
        void losErroresLlevanElPrefijoError() {
            Guion guion = new Guion();
            guion.consola.error("Credenciales incorrectas");
            assertEquals("ERROR: Credenciales incorrectas", guion.salida().trim());
        }

        @Test
        void formularioDeNuevoPaisAceptaLaRegionPorNumero() {
            Guion guion = new Guion("Ecuador", "Quito", "1", "South America", "17.643.060", "276841,5", "-", "s");
            Pais pais = new PaisConsola(guion.consola, sesionActiva()).leerPais(null);
            assertEquals("Ecuador", pais.getNombre());
            assertEquals("América", pais.getRegion());
            assertEquals(17_643_060, pais.getPoblacion());
            assertEquals(276_841.5, pais.getArea());
            assertNull(pais.getMoneda());
        }

        @Test
        void enterConservaLosValoresActuales() {
            Pais actual = new Pais("Perú", "Lima", "América", "South America", 32_971_846, 1_285_216, "Sol (PEN)");
            actual.setId(3);
            Guion guion = new Guion("", "", "europa", "", "", "", "", "si");
            Pais pais = new PaisConsola(guion.consola, sesionActiva()).leerPais(actual);
            assertEquals("Perú", pais.getNombre());
            assertEquals("Europa", pais.getRegion());
            assertEquals(32_971_846, pais.getPoblacion());
            assertEquals(1_285_216, pais.getArea());
            assertTrue(guion.salida().contains("Datos del país"));
            assertTrue(guion.salida().contains("Capital [Lima]"));
        }

        @Test
        void responderNoCancelaElFormulario() {
            Guion guion = new Guion("Chile", "", "1", "", "100", "", "", "n");
            assertNull(new PaisConsola(guion.consola, sesionActiva()).leerPais(null));
        }

        @Test
        void unaRegionQueNoExisteQuedaVaciaParaQueLaValideElControlador() {
            assertNull(regionDe("9"));
            assertNull(regionDe("Atlántida"));
            assertEquals("Oceanía", regionDe("5"));
        }

        private String regionDe(String respuesta) {
            Guion guion = new Guion("X", "", respuesta, "", "1", "", "", "s");
            return new PaisConsola(guion.consola, sesionActiva()).leerPais(null).getRegion();
        }

        @Test
        void elPaisSeEligePorElIdDeLaUltimaTabla() {
            Guion guion = new Guion("7", "99", "abc");
            PaisConsola vista = new PaisConsola(guion.consola, sesionActiva());
            Pais peru = new Pais("Perú", "Lima", "América", null, 1, 1, null);
            peru.setId(7);
            vista.mostrarTabla(List.of(peru));
            assertEquals("Perú", vista.paisSeleccionado().getNombre());
            assertNull(vista.paisSeleccionado());
            assertNull(vista.paisSeleccionado());
            assertTrue(guion.salida().contains("|  7 | Perú"));
        }

        @Test
        void busquedaConRegionYCriteriosVacios() {
            Guion guion = new Guion("", "lim", "8", "1");
            BusquedaConsola vista = new BusquedaConsola(guion.consola, sesionActiva());
            vista.mostrarFiltrosRegion(Pais.REGIONES);
            CriteriosBusqueda criterios = vista.leerCriterios();
            assertNull(criterios.getNombre());
            assertEquals("lim", criterios.getCapital());
            assertEquals("América", criterios.getRegion());
            assertTrue(guion.salida().contains("ERROR: Opción no válida"));
        }

        @Test
        void escribirCeroComoUsuarioTerminaElPrograma() {
            LoginConsola login = new LoginConsola(new Guion("0").consola, new SesionServicio());
            assertThrows(Consola.Salida.class, login::leerCredenciales);
        }

        @Test
        void siSeAcabaLaEntradaTerminaElPrograma() {
            Consola consola = new Guion().consola;
            assertThrows(Consola.Salida.class, () -> consola.leer("Usuario"));
        }
    }

    @Nested
    class Flujo {

        @Test
        void inicioFallidoInicioCorrectoReporteYCierreDeSesion() throws IOException {
            try (ServidorFalso servidor = new ServidorFalso()) {
                // El primer intento falla y el segundo entra.
                servidor.alPrimerPedido("POST /Login/login", 401,
                        "{\"estado\":\"ERROR\",\"mensaje\":\"Credenciales incorrectas\",\"datos\":null}");
                servidor.ok("POST /Login/login", "Ingreso de sesión exitoso",
                        "{\"token\":\"t-9\",\"nombre\":\"Marcos Escobar\"}");
                servidor.ok("GET /Reportes", "Reportes generados correctamente",
                        "{\"paisesPorRegion\":[{\"region\":\"América\",\"cantidad\":1}],\"paisMasPoblado\":" + PERU
                                + ",\"promedioPorRegion\":[{\"region\":\"América\",\"promedioPoblacion\":32971846}]}");
                servidor.ok("POST /Login/logout", "Sesión cerrada", "null");
                Guion guion = new Guion("monster", "mala", "monster", "monster9", "9", "5", "7", "0");

                new App(guion.consola, new ClienteRest(servidor.urlBase(), 5000)).ejecutar();

                String salida = guion.salida();
                assertTrue(salida.contains("ERROR: Credenciales incorrectas"), salida);
                assertTrue(salida.contains("Bienvenido, Marcos Escobar."), salida);
                assertTrue(salida.contains("ERROR: Opción no válida"), salida);
                assertTrue(salida.contains("Perú · 32.971.846 habitantes · América"), salida);
                assertTrue(salida.contains("Servidor disponible"), salida);
                assertTrue(salida.trim().endsWith("Hasta luego."), salida);
                assertEquals("t-9", servidor.ultima().authorization().replace("Bearer ", ""));
                // El encabezado del inicio de sesión: al entrar y después de cerrar sesión.
                assertEquals(2, contar(salida, "CountryAPI · Iniciar sesión"));
            }
        }

        @Test
        void siLaSesionExpiraVuelveAlInicioDeSesion() throws IOException {
            try (ServidorFalso servidor = new ServidorFalso()) {
                servidor.ok("POST /Login/login", "Ingreso de sesión exitoso",
                        "{\"token\":\"t-9\",\"nombre\":\"Marcos Escobar\"}");
                servidor.error("GET /Paises", 401, "Token inválido o sesión expirada");
                Guion guion = new Guion("monster", "monster9", "3", "0");
                new App(guion.consola, new ClienteRest(servidor.urlBase(), 5000)).ejecutar();

                String salida = guion.salida();
                assertTrue(salida.contains("ERROR: " + ClienteRest.SESION_EXPIRADA), salida);
                assertEquals(2, contar(salida, "CountryAPI · Iniciar sesión"));
                assertFalse(salida.contains("1. Listar todos"), "No debe seguir en Países: " + salida);
            }
        }

        private int contar(String texto, String buscado) {
            int veces = 0;
            for (int i = texto.indexOf(buscado); i >= 0; i = texto.indexOf(buscado, i + 1)) {
                veces++;
            }
            return veces;
        }
    }
}
