package ec.edu.monster.ws;

import ec.edu.monster.controladores.UniversidadControlador;
import ec.edu.monster.modelos.dto.RespuestaDTO;
import ec.edu.monster.servicios.TokenService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de universidades: /api/Universidades (ECUD10).
 *
 * @author Escobar Marcos
 */
@Path("/Universidades")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
public class UniversidadWebService {

    private static final Logger LOG = Logger.getLogger(UniversidadWebService.class.getName());

    private final UniversidadControlador controlador;
    private final TokenService tokenService;

    public UniversidadWebService() {
        this.controlador = new UniversidadControlador();
        this.tokenService = TokenService.getInstancia();
    }

    /** POST /api/Universidades/importar?pais=Ecuador: importa las universidades del país. */
    @POST
    @Path("/importar")
    public Response importar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @QueryParam("pais") String pais) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            return Response.ok(RespuestaDTO.ok("Importación de universidades finalizada",
                    controlador.importarUniversidades(pais))).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /** GET /api/Universidades?pais=Ecuador: universidades guardadas del país. */
    @GET
    public Response listar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @QueryParam("pais") String pais) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            return Response.ok(RespuestaDTO.ok("Consulta realizada correctamente",
                    controlador.listarUniversidades(pais))).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    private static Response tokenInvalido() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(RespuestaDTO.error(RespuestaDTO.TOKEN_INVALIDO)).build();
    }

    private static Response error(Exception e) {
        if (RespuestaDTO.esErrorInterno(e)) {
            LOG.log(Level.SEVERE, "Error en /Universidades", e);
        }
        return Response.status(RespuestaDTO.codigoHttp(e)).entity(RespuestaDTO.deExcepcion(e)).build();
    }
}
