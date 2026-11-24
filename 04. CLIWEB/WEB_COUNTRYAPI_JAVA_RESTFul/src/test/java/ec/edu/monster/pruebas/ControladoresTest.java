package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.controlador.UniversidadControlador;
import ec.edu.monster.dobles.ServidorFalso;
import ec.edu.monster.dobles.VistaFalsa;
import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.servicios.LoginServicio;
import ec.edu.monster.servicios.PaisServicio;
import ec.edu.monster.servicios.ReporteServicio;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.servicios.UniversidadServicio;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Controladores del cliente con una vista en memoria y un servidor RESTful falso.
 */
class ControladoresTest {

    private static final String PERU = "{\"id\":3,\"nombre\":\"Perú\",\"capital\":\"Lima\",\"region\":\"América\","
            + "\"subregion\":\"South America\",\"poblacion\":32971846,\"area\":1285216.0,"
            + "\"moneda\":\"Peruvian sol (PEN)\",\"guardado\":true}";

    private ServidorFalso servidor;
    private ClienteRest cliente;
    private final SesionServicio sesion = new SesionServicio();
    private final VistaFalsa vista = new VistaFalsa();

    @BeforeEach
    void iniciar() throws IOException {
        servidor = new ServidorFalso();
        cliente = new ClienteRest(servidor.urlBase(), 5000);
        sesion.guardar("t-1", "Marcos Escobar");
    }

    @AfterEach
    void detener() {
        servidor.close();
    }

    private static Pais pais(String nombre, String region, long poblacion, double area) {
        return new Pais(nombre, "Capital", region, "Sub", poblacion, area, "Moneda");
    }

    @Nested
    class Login {

        private LoginControlador controlador;

        @BeforeEach
        void crear() {
            sesion.cerrar();
            controlador = new LoginControlador(vista, vista, new LoginServicio(cliente), sesion);
        }

        @Test
        void camposVaciosNoLlamanAlServidor() {
            vista.usuario = " ";
            vista.clave = "x";
            controlador.iniciarSesion();
            assertEquals("Ingrese el usuario", vista.ultimoError());
            assertTrue(servidor.peticiones().isEmpty());
        }

        @Test
        void credencialesIncorrectasBorranLaClave() {
            servidor.error("POST /Login/login", 401, "Credenciales incorrectas");
            vista.usuario = "monster";
            vista.clave = "mala";
            controlador.iniciarSesion();
            assertEquals("Credenciales incorrectas", vista.ultimoError());
            assertTrue(vista.claveLimpiada);
            assertNull(vista.menuMostradoPara);
            assertFalse(sesion.estaActiva());
        }

        @Test
        void sinConexionConservaLosDatos() {
            LoginControlador sinServidor = new LoginControlador(vista, vista,
                    new LoginServicio(new ClienteRest("http://127.0.0.1:9/api", 2000)), sesion);
            vista.usuario = "monster";
            vista.clave = "monster9";
            sinServidor.iniciarSesion();
            assertEquals(ClienteRest.SIN_CONEXION, vista.ultimoError());
            assertFalse(vista.claveLimpiada);
        }

        @Test
        void credencialesCorrectasGuardanLaSesionYMuestranElMenu() {
            servidor.ok("POST /Login/login", "Ingreso de sesión exitoso", "{\"token\":\"t-9\",\"nombre\":\"Marcos Escobar\"}");
            vista.usuario = " monster ";
            vista.clave = "monster9";
            controlador.iniciarSesion();
            assertEquals("t-9", sesion.getToken());
            assertEquals("Marcos Escobar", vista.menuMostradoPara);
            assertTrue(servidor.ultima().cuerpo().contains("\"usuario\":\"monster\""), "se envía sin espacios");
        }

        @Test
        void cerrarSesionAvisaAlServidorYVuelveAlLogin() {
            servidor.ok("POST /Login/logout", "Sesión cerrada", "null");
            sesion.guardar("t-9", "Marcos Escobar");
            controlador.cerrarSesion();
            assertEquals("Bearer t-9", servidor.ultima().authorization());
            assertFalse(sesion.estaActiva());
            assertEquals(1, vista.vecesInicioSesion);
            assertEquals(1, vista.vecesFormularioLogin);
        }

        @Test
        void cerrarSesionFuncionaAunqueElServidorNoResponda() {
            LoginControlador sinServidor = new LoginControlador(vista, vista,
                    new LoginServicio(new ClienteRest("http://127.0.0.1:9/api", 2000)), sesion);
            sesion.guardar("t-9", "Marcos Escobar");
            sinServidor.cerrarSesion();
            assertFalse(sesion.estaActiva());
            assertEquals(1, vista.vecesInicioSesion);
        }
    }

    @Nested
    class Paises {

        private PaisControlador controlador;

        @BeforeEach
        void crear() {
            controlador = new PaisControlador(vista, vista, new PaisServicio(cliente), sesion);
            servidor.ok("GET /Paises", "Consulta realizada correctamente", "[" + PERU + "]");
        }

        @Test
        void muestraLosFiltrosDeRegionAlCrearse() {
            assertEquals(Pais.REGIONES, vista.filtrosRegion);
        }

        @Test
        void listarMuestraLaTabla() {
            controlador.listar();
            assertEquals("Perú", vista.tabla.get(0).getNombre());
            assertEquals("Bearer t-1", servidor.ultima().authorization());
        }

        @Test
        void tablaVaciaSugiereImportar() {
            servidor.ok("GET /Paises", "Consulta realizada correctamente", "[]");
            controlador.listar();
            assertEquals("No hay países registrados. Use «Importar países» para cargarlos", vista.ultimoMensaje());
        }

        @Test
        void sesionExpiradaVuelveAlInicioDeSesion() {
            servidor.error("GET /Paises", 401, "Token inválido o sesión expirada");
            controlador.listar();
            assertEquals(ClienteRest.SESION_EXPIRADA, vista.ultimoError());
            assertFalse(sesion.estaActiva());
            assertEquals(1, vista.vecesInicioSesion);
        }

        @Test
        void buscarEnviaElTextoYAvisaSiNoHayResultados() {
            servidor.ok("GET /Paises", "Consulta realizada correctamente", "[]");
            vista.filtro = "zz";
            controlador.buscar();
            assertEquals("/Paises?nombre=zz", servidor.ultima().ruta());
            assertEquals("No se encontraron países", vista.ultimoMensaje());
        }

        @Test
        void registrarReabreElFormularioSiLosDatosSonInvalidos() {
            servidor.responder("POST /Paises", 201,
                    "{\"estado\":\"OK\",\"mensaje\":\"País registrado correctamente\",\"datos\":{\"id\":20}}");
            Pais invalido = pais("Chile", "América", -1, 10);
            Pais valido = pais("Chile", "América", 19116209, 756102);
            vista.respuestasFormulario.add(invalido);
            vista.respuestasFormulario.add(valido);

            controlador.registrar();

            assertEquals("La población debe ser un número entero mayor o igual que cero", vista.errores.get(0));
            assertNull(vista.formulariosAbiertos.get(0), "el primero se abre vacío");
            assertSame(invalido, vista.formulariosAbiertos.get(1), "el segundo conserva lo escrito");
            assertEquals(1, servidor.peticiones().stream().filter(p -> p.metodo().equals("POST")).count());
            assertEquals("País registrado correctamente", vista.ultimoMensaje());
            assertEquals("Perú", vista.tabla.get(0).getNombre(), "la tabla se recarga");
        }

        @Test
        void registrarUnNombreRepetidoMuestraElErrorYPermiteCorregir() {
            servidor.error("POST /Paises", 409, "Ya existe un país con ese nombre");
            vista.respuestasFormulario.add(pais("Perú", "América", 1, 1));
            vista.respuestasFormulario.add(null);

            controlador.registrar();

            assertEquals("Ya existe un país con ese nombre", vista.ultimoError());
            assertEquals(2, vista.formulariosAbiertos.size(), "se reabre y el usuario cancela");
        }

        @Test
        void actualizarEnviaPutConElIdDelSeleccionado() {
            servidor.ok("PUT /Paises/3", "País actualizado correctamente", PERU);
            Pais seleccionado = pais("Perú", "América", 1, 1);
            seleccionado.setId(3);
            vista.seleccionado = seleccionado;
            vista.respuestasFormulario.add(pais("Perú", "América", 33000000, 1285216));

            controlador.actualizar();

            assertEquals("PUT", servidor.peticiones().get(0).metodo());
            assertEquals("País actualizado correctamente", vista.ultimoMensaje());
            assertEquals(3, vista.formulariosAbiertos.get(0).getId());
        }

        @Test
        void editarOEliminarSinSeleccionPideElegirUnPais() {
            controlador.actualizar();
            controlador.eliminar();
            assertEquals(2, vista.errores.stream().filter("Seleccione un país de la tabla"::equals).count());
            assertTrue(servidor.peticiones().isEmpty());
        }

        @Test
        void eliminarSinConfirmarNoEnviaNada() {
            Pais seleccionado = pais("Perú", "América", 1, 1);
            seleccionado.setId(3);
            vista.seleccionado = seleccionado;
            vista.confirmar = false;
            controlador.eliminar();
            assertSame(seleccionado, vista.confirmacionPedidaPara);
            assertTrue(servidor.peticiones().isEmpty());
        }

        @Test
        void eliminarConfirmadoEnviaDeleteYRecarga() {
            servidor.ok("DELETE /Paises/3", "País eliminado correctamente", "null");
            Pais seleccionado = pais("Perú", "América", 1, 1);
            seleccionado.setId(3);
            vista.seleccionado = seleccionado;
            controlador.eliminar();
            assertEquals("DELETE", servidor.peticiones().get(0).metodo());
            assertEquals("País eliminado correctamente", vista.ultimoMensaje());
        }

        @Test
        void buscarYFiltrarMuestraElTotalOAvisaSinResultados() {
            servidor.ok("GET /Paises/buscar", "Búsqueda realizada correctamente", "[" + PERU + "]");
            vista.criterios = new CriteriosBusqueda(null, "lima", "América");
            controlador.buscarYFiltrar();
            assertEquals(1, vista.totalBusqueda);
            assertEquals("/Paises/buscar?capital=lima&region=Am%C3%A9rica", servidor.ultima().ruta());

            servidor.ok("GET /Paises/buscar", "Búsqueda realizada correctamente", "[]");
            controlador.buscarYFiltrar();
            assertEquals("No se encontraron países con esos criterios", vista.ultimoMensaje());
        }
    }

    @Nested
    class Api {

        private ApiControlador controlador;

        @BeforeEach
        void crear() {
            controlador = new ApiControlador(vista, vista, new PaisServicio(cliente), sesion);
        }

        @Test
        void importarCanceladoNoEnviaNada() {
            vista.confirmarImportacion = false;
            controlador.importar();
            assertTrue(servidor.peticiones().isEmpty());
        }

        @Test
        void importarMuestraElResumen() {
            servidor.ok("POST /Paises/importar", "Importación finalizada",
                    "{\"recibidos\":254,\"registrados\":242,\"actualizados\":12}");
            controlador.importar();
            assertEquals(254, vista.resumen.getRecibidos());
            assertEquals(12, vista.resumen.getActualizados());
        }

        @Test
        void consultarExigeDosCaracteres() {
            vista.nombreConsulta = "e";
            controlador.consultar();
            assertEquals("Ingrese al menos dos caracteres", vista.ultimoError());
            assertTrue(servidor.peticiones().isEmpty());
        }

        @Test
        void consultarSinCoincidenciasMuestraElMensajeDelServidor() {
            servidor.error("GET /Paises/externo/zzz", 404, "No se encontraron países con ese nombre en la API");
            vista.nombreConsulta = "zzz";
            controlador.consultar();
            assertEquals("No se encontraron países con ese nombre en la API", vista.ultimoError());
        }

        @Test
        void guardarMarcaElPaisComoGuardado() {
            servidor.ok("GET /Paises/externo/chile", "Consulta en la API exitosa",
                    "[{\"nombre\":\"Chile\",\"region\":\"América\",\"poblacion\":1,\"area\":1,\"guardado\":false}]");
            servidor.ok("POST /Paises/guardar", "País guardado correctamente", "{\"id\":40,\"nombre\":\"Chile\"}");
            vista.nombreConsulta = "chile";
            controlador.consultar();
            Pais chile = vista.resultadosConsulta.get(0);
            assertFalse(chile.isGuardado());

            vista.seleccionConsulta = chile;
            controlador.guardar();

            assertTrue(vista.resultadosConsulta.get(0).isGuardado());
            assertEquals("País guardado correctamente", vista.ultimoMensaje());
        }

        @Test
        void guardarSinSeleccionPideElegirUnPais() {
            controlador.guardar();
            assertEquals("Seleccione un país de la tabla", vista.ultimoError());
        }
    }

    @Nested
    class Reportes {

        @Test
        void sinPaisesSugiereImportar() {
            servidor.error("GET /Reportes", 404, "No hay países registrados para generar reportes");
            new ReporteControlador(vista, new ReporteServicio(cliente), sesion).generar();
            assertEquals("No hay países registrados para generar reportes. Use «Importar países» para cargarlos.",
                    vista.ultimoError());
        }

        @Test
        void muestraElReporte() {
            servidor.ok("GET /Reportes", "Reportes generados correctamente",
                    "{\"paisesPorRegion\":[],\"paisMasPoblado\":" + PERU + ",\"promedioPorRegion\":[]}");
            new ReporteControlador(vista, new ReporteServicio(cliente), sesion).generar();
            assertEquals("Perú", vista.reporte.getPaisMasPoblado().getNombre());
        }
    }

    @Nested
    class Universidades {

        private UniversidadControlador controlador;

        @BeforeEach
        void crear() {
            controlador = new UniversidadControlador(vista, new UniversidadServicio(cliente), sesion);
        }

        @Test
        void paisVacioNoLlamaAlServidor() {
            vista.paisUniversidades = " ";
            controlador.importar();
            assertEquals("Ingrese el país", vista.ultimoError());
            assertTrue(servidor.peticiones().isEmpty());
        }

        @Test
        void importarMuestraElResumenYRecargaLaTabla() {
            servidor.ok("POST /Universidades/importar", "Importación de universidades finalizada",
                    "{\"recibidos\":43,\"registrados\":38,\"actualizados\":5}");
            servidor.ok("GET /Universidades", "Consulta realizada correctamente",
                    "[{\"nombre\":\"Escuela Politécnica del Ejército\",\"dominio\":\"espe.edu.ec\"}]");
            vista.paisUniversidades = "Ecuador";
            controlador.importar();
            assertEquals(38, vista.resumen.getRegistrados());
            assertEquals("espe.edu.ec", vista.universidades.get(0).getDominio());
            assertEquals("/Universidades?pais=Ecuador", servidor.ultima().ruta());
        }

        @Test
        void listarVacioAvisa() {
            servidor.ok("GET /Universidades", "Consulta realizada correctamente", "[]");
            vista.paisUniversidades = "Narnia";
            controlador.listar();
            assertEquals("No hay universidades guardadas para ese país", vista.ultimoMensaje());
        }
    }
}
