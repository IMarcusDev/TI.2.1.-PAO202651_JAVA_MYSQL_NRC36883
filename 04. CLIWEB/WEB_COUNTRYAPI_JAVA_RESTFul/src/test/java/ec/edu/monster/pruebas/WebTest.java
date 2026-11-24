package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ec.edu.monster.dobles.Navegador;
import ec.edu.monster.dobles.ServidorFalso;
import ec.edu.monster.filtros.AuthFilter;
import ec.edu.monster.filtros.CsrfFilter;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.vista.PaisVistaWeb;
import ec.edu.monster.vista.VistaWeb;
import ec.edu.monster.web.ConsultarServlet;
import ec.edu.monster.web.LoginServlet;
import ec.edu.monster.web.LogoutServlet;
import ec.edu.monster.web.ModulosSesion;
import ec.edu.monster.web.PaisesServlet;
import ec.edu.monster.web.UniversidadesServlet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Servlets y filtros del cliente web con un navegador simulado (Mockito) y el servidor RESTful
 * falso: inicio de sesión, CSRF, control de acceso, CRUD de países con el formulario que se
 * reabre, consulta y guardado sin repetir la consulta, y sesión expirada.
 */
class WebTest {

    private static final String PERU = "{\"id\":3,\"nombre\":\"Perú\",\"capital\":\"Lima\",\"region\":\"América\","
            + "\"subregion\":\"South America\",\"poblacion\":32971846,\"area\":1285216.0,"
            + "\"moneda\":\"Peruvian sol (PEN)\"}";

    private ServidorFalso servidor;
    private Navegador navegador;

    @BeforeEach
    void iniciar() throws IOException {
        servidor = new ServidorFalso();
        navegador = new Navegador(new ClienteRest(servidor.urlBase(), 5000));
    }

    @AfterEach
    void detener() {
        servidor.close();
    }

    private Navegador.Resultado enviar(HttpServlet servlet, String metodo, String ruta, String... parametros)
            throws Exception {
        Navegador.Resultado r = navegador.peticion(metodo, ruta, parametros);
        servlet.service(r.peticion, r.respuesta);
        return r;
    }

    private void iniciarSesion() throws Exception {
        servidor.ok("POST /Login/login", "Ingreso de sesión exitoso", "{\"token\":\"t-9\",\"nombre\":\"Marcos Escobar\"}");
        enviar(new LoginServlet(), "POST", "/login", "usuario", "monster", "clave", "monster9");
    }

    @Nested
    class InicioDeSesion {

        @Test
        void credencialesCorrectasCambianElIdYLlevanAlMenuConSaludo() throws Exception {
            servidor.ok("POST /Login/login", "Ingreso de sesión exitoso", "{\"token\":\"t-9\",\"nombre\":\"Marcos Escobar\"}");
            Navegador.Resultado r = enviar(new LoginServlet(), "POST", "/login", "usuario", " monster ", "clave", "monster9");
            assertEquals("/web/menu", r.redireccion);
            assertTrue(r.idCambiado, "Debe cambiar el id de sesión al autenticarse");
            assertEquals("Bienvenido, Marcos Escobar.", navegador.enSesion("flash.mensaje"));
            ModulosSesion modulos = (ModulosSesion) navegador.enSesion(ModulosSesion.ATRIBUTO);
            assertEquals("t-9", modulos.getSesion().getToken());
            assertTrue(servidor.ultima().cuerpo().contains("\"usuario\":\"monster\""));
        }

        @Test
        void credencialesIncorrectasVuelvenAlFormularioConElError() throws Exception {
            servidor.error("POST /Login/login", 401, "Credenciales incorrectas");
            Navegador.Resultado r = enviar(new LoginServlet(), "POST", "/login", "usuario", "monster", "clave", "mala");
            assertEquals("/WEB-INF/vista/login.jsp", r.jsp);
            assertEquals("Credenciales incorrectas", r.atributo(VistaWeb.ERROR));
            assertNull(r.redireccion);
        }

        @Test
        void sinUsuarioMuestraElMensajeDelEcudSinLlamarAlServidor() throws Exception {
            Navegador.Resultado r = enviar(new LoginServlet(), "POST", "/login", "usuario", "", "clave", "x");
            assertEquals("Ingrese el usuario", r.atributo(VistaWeb.ERROR));
            assertTrue(servidor.peticiones().isEmpty());
        }

        @Test
        void conSesionAbiertaElLoginLlevaAlMenu() throws Exception {
            iniciarSesion();
            assertEquals("/web/menu", enviar(new LoginServlet(), "GET", "/login").redireccion);
        }

        @Test
        void cerrarSesionAvisaAlServidorEInvalidaLaSesion() throws Exception {
            iniciarSesion();
            servidor.ok("POST /Login/logout", "Sesión cerrada", "null");
            Navegador.Resultado r = enviar(new LogoutServlet(), "POST", "/logout");
            assertEquals("/web/login", r.redireccion);
            assertTrue(navegador.sesionInvalidada());
            assertEquals("POST", servidor.ultima().metodo());
            assertEquals("/Login/logout", servidor.ultima().ruta());
        }
    }

    @Nested
    class Filtros {

        @Test
        void sinSesionLasPantallasProtegidasLlevanAlLogin() throws Exception {
            Navegador.Resultado r = navegador.peticion("GET", "/paises");
            FilterChain cadena = mock(FilterChain.class);
            new AuthFilter().doFilter(r.peticion, r.respuesta, cadena);
            assertEquals("/web/login", r.redireccion);
            verify(cadena, never()).doFilter(r.peticion, r.respuesta);
        }

        @Test
        void conSesionPasaAlServlet() throws Exception {
            iniciarSesion();
            Navegador.Resultado r = navegador.peticion("GET", "/paises");
            FilterChain cadena = mock(FilterChain.class);
            new AuthFilter().doFilter(r.peticion, r.respuesta, cadena);
            verify(cadena).doFilter(r.peticion, r.respuesta);
        }

        @Test
        void unPostSinTokenCsrfSeRechaza() throws Exception {
            iniciarSesion();
            Navegador.Resultado r = navegador.peticion("POST", "/paises", "accion", "eliminar", "id", "3");
            FilterChain cadena = mock(FilterChain.class);
            new CsrfFilter().doFilter(r.peticion, r.respuesta, cadena);
            assertEquals(403, r.error);
            verify(cadena, never()).doFilter(r.peticion, r.respuesta);
        }

        @Test
        void unPostConElTokenDeLaSesionPasa() throws Exception {
            Navegador.Resultado get = navegador.peticion("GET", "/login");
            new CsrfFilter().doFilter(get.peticion, get.respuesta, mock(FilterChain.class));
            String token = (String) navegador.enSesion(CsrfFilter.ATRIBUTO);
            assertNotNull(token, "GET /login debe crear el token");

            Navegador.Resultado post = navegador.peticion("POST", "/login", CsrfFilter.PARAMETRO, token);
            FilterChain cadena = mock(FilterChain.class);
            new CsrfFilter().doFilter(post.peticion, post.respuesta, cadena);
            verify(cadena).doFilter(post.peticion, post.respuesta);
        }

        @Test
        void unLoginConTokenVencidoMuestraUnFormularioNuevo() throws Exception {
            Navegador.Resultado r = navegador.peticion("POST", "/login", CsrfFilter.PARAMETRO, "otro");
            new CsrfFilter().doFilter(r.peticion, r.respuesta, mock(FilterChain.class));
            assertEquals("/web/login", r.redireccion);
        }
    }

    @Nested
    class Paises {

        @BeforeEach
        void conSesion() throws Exception {
            iniciarSesion();
            servidor.ok("GET /Paises", "Consulta realizada correctamente", "[" + PERU + "]");
        }

        @Test
        void listaLosPaisesYAbreElFormularioDeEdicion() throws Exception {
            Navegador.Resultado r = enviar(new PaisesServlet(), "GET", "/paises", "editar", "3");
            assertEquals("/WEB-INF/vista/paises.jsp", r.jsp);
            assertEquals(1, ((List<?>) r.atributo(PaisVistaWeb.PAISES)).size());
            assertEquals("Lima", ((Pais) r.atributo(PaisVistaWeb.FORMULARIO)).getCapital());
            assertEquals(Pais.REGIONES, r.atributo("regiones"));
        }

        @Test
        void datosInvalidosReabrenElFormularioConLoEscrito() throws Exception {
            Navegador.Resultado r = enviar(new PaisesServlet(), "POST", "/paises",
                    "accion", "registrar", "nombre", "Chile", "region", "América", "poblacion", "abc");
            assertEquals("/WEB-INF/vista/paises.jsp", r.jsp);
            assertEquals("La población debe ser un número entero mayor o igual que cero", r.atributo(VistaWeb.ERROR));
            assertEquals("Chile", ((Pais) r.atributo(PaisVistaWeb.FORMULARIO)).getNombre());
            assertFalse(servidor.peticiones().stream().anyMatch(p -> p.metodo().equals("POST") && p.ruta().equals("/Paises")));
        }

        @Test
        void unDuplicadoDelServidorTambienReabreElFormulario() throws Exception {
            servidor.error("POST /Paises", 409, "Ya existe un país con ese nombre");
            Navegador.Resultado r = enviar(new PaisesServlet(), "POST", "/paises",
                    "accion", "registrar", "nombre", "Perú", "region", "América", "poblacion", "1");
            assertEquals("Ya existe un país con ese nombre", r.atributo(VistaWeb.ERROR));
            assertNotNull(r.atributo(PaisVistaWeb.FORMULARIO));
        }

        @Test
        void registrarBienRedirigeALaListaConElMensaje() throws Exception {
            servidor.responder("POST /Paises", 201,
                    "{\"estado\":\"OK\",\"mensaje\":\"País registrado correctamente\",\"datos\":" + PERU + "}");
            Navegador.Resultado r = enviar(new PaisesServlet(), "POST", "/paises",
                    "accion", "registrar", "nombre", "Perú", "region", "América", "poblacion", "32.971.846",
                    "area", "1285216,5");
            assertEquals("/web/paises", r.redireccion);
            assertEquals("País registrado correctamente", navegador.enSesion("flash.mensaje"));
            String cuerpo = servidor.peticiones().stream()
                    .filter(p -> p.metodo().equals("POST") && p.ruta().equals("/Paises"))
                    .findFirst().orElseThrow().cuerpo();
            assertTrue(cuerpo.contains("\"poblacion\":32971846") && cuerpo.contains("1285216.5"), cuerpo);
        }

        @Test
        void eliminarSinConfirmarNoLlamaAlServidor() throws Exception {
            enviar(new PaisesServlet(), "GET", "/paises");
            enviar(new PaisesServlet(), "POST", "/paises", "accion", "eliminar", "id", "3");
            assertFalse(servidor.peticiones().stream().anyMatch(p -> p.metodo().equals("DELETE")));
        }

        @Test
        void eliminarConfirmadoUsaElIdDeLaTabla() throws Exception {
            servidor.ok("DELETE /Paises/3", "País eliminado correctamente", "null");
            enviar(new PaisesServlet(), "GET", "/paises");
            Navegador.Resultado r = enviar(new PaisesServlet(), "POST", "/paises",
                    "accion", "eliminar", "id", "3", "confirmado", "si");
            assertEquals("/web/paises", r.redireccion);
            assertEquals("País eliminado correctamente", navegador.enSesion("flash.mensaje"));
        }

        @Test
        void siElServidorRechazaElTokenVuelveAlLoginConElAviso() throws Exception {
            servidor.error("GET /Paises", 401, "Token inválido o sesión expirada");
            Navegador.Resultado r = enviar(new PaisesServlet(), "GET", "/paises");
            assertEquals("/web/login", r.redireccion);
            assertTrue(navegador.sesionInvalidada());
            assertEquals(ClienteRest.SESION_EXPIRADA, navegador.enSesion("flash.error"));
            assertNull(navegador.enSesion(ModulosSesion.ATRIBUTO));
        }
    }

    @Nested
    class ConsultaYUniversidades {

        @BeforeEach
        void conSesion() throws Exception {
            iniciarSesion();
        }

        @Test
        void guardarUsaLaFilaElegidaSinVolverAConsultarLaApi() throws Exception {
            servidor.ok("GET /Paises/externo/peru", "Consulta en la API exitosa", "[" + PERU + "]");
            servidor.ok("POST /Paises/guardar", "País guardado correctamente", PERU);
            enviar(new ConsultarServlet(), "GET", "/consultar", "nombre", "peru");

            Navegador.Resultado guardar = enviar(new ConsultarServlet(), "POST", "/consultar", "numero", "1");
            assertEquals("/web/consultar", guardar.redireccion);
            assertTrue(servidor.ultima().cuerpo().contains("\"nombre\":\"Perú\""));

            Navegador.Resultado despues = enviar(new ConsultarServlet(), "GET", "/consultar");
            List<?> resultados = (List<?>) despues.atributo("resultados");
            assertTrue(((Pais) resultados.get(0)).isGuardado());
            assertEquals(1, servidor.peticiones().stream().filter(p -> p.ruta().startsWith("/Paises/externo")).count(),
                    "Solo una consulta a REST Countries");
            assertEquals("País guardado correctamente", despues.atributo(VistaWeb.MENSAJE));
        }

        @Test
        void verGuardadasSoloCambiaDePais() throws Exception {
            Navegador.Resultado r = enviar(new UniversidadesServlet(), "POST", "/universidades",
                    "accion", "listar", "pais", "United States");
            assertEquals("/web/universidades?pais=United%20States", r.redireccion);
            assertTrue(servidor.peticiones().stream().noneMatch(p -> p.ruta().startsWith("/Universidades")));
        }

        @Test
        void sinPaisSeMuestranLasDeEcuador() throws Exception {
            servidor.ok("GET /Universidades", "Consulta realizada correctamente", "[]");
            Navegador.Resultado r = enviar(new UniversidadesServlet(), "GET", "/universidades");
            assertEquals("Ecuador", r.atributo("pais"));
            assertEquals("/Universidades?pais=Ecuador", servidor.ultima().ruta());
        }
    }
}
