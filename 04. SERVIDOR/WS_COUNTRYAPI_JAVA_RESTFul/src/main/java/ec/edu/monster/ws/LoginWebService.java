package ec.edu.monster.ws;

import ec.edu.monster.controladores.LoginControlador;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.dto.LoginRequestDTO;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.dto.RespuestaDTO;
import ec.edu.monster.servicios.TokenService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de autenticación: /api/Login/login y /api/Login/logout (ECUD1).
 *
 * @author Escobar Marcos
 */
@Path("/Login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
public class LoginWebService {

    private static final Logger LOG = Logger.getLogger(LoginWebService.class.getName());

    private final LoginControlador controlador;
    private final TokenService tokenService;

    public LoginWebService() {
        this.controlador = new LoginControlador();
        this.tokenService = TokenService.getInstancia();
    }

    /** POST /api/Login/login con {usuario, clave}: devuelve {token, nombre} o 401. */
    @POST
    @Path("/login")
    public Response login(LoginRequestDTO credenciales) {
        try {
            Sesion sesion = credenciales == null
                    ? controlador.autenticar(null, null)
                    : controlador.autenticar(credenciales.getUsuario(), credenciales.getClave());
            LoginResponseDTO datos = new LoginResponseDTO(sesion.getToken(), sesion.getUsuario().getNombre());
            return Response.ok(RespuestaDTO.ok("Ingreso de sesión exitoso", datos)).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /**
     * POST /api/Login/logout con el token en Authorization. Responde OK aunque el token ya no
     * exista: el cliente vuelve al inicio de sesión de todas formas (ECUD1, flujo alternativo 4).
     */
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
        tokenService.eliminarToken(authorization);
        return Response.ok(RespuestaDTO.ok("Sesión cerrada", null)).build();
    }

    private static Response error(Exception e) {
        if (RespuestaDTO.esErrorInterno(e)) {
            LOG.log(Level.SEVERE, "Error en /Login", e);
        }
        return Response.status(RespuestaDTO.codigoHttp(e)).entity(RespuestaDTO.deExcepcion(e)).build();
    }
}
