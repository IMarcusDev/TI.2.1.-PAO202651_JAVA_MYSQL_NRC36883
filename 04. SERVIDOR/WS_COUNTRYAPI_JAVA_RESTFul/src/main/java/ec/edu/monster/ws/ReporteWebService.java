package ec.edu.monster.ws;

import ec.edu.monster.controladores.ReporteControlador;
import ec.edu.monster.modelos.dto.RespuestaDTO;
import ec.edu.monster.servicios.TokenService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de reportes: GET /api/Reportes (ECUD6).
 *
 * @author Escobar Marcos
 */
@Path("/Reportes")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
public class ReporteWebService {

    private static final Logger LOG = Logger.getLogger(ReporteWebService.class.getName());

    private final ReporteControlador controlador;
    private final TokenService tokenService;

    public ReporteWebService() {
        this.controlador = new ReporteControlador();
        this.tokenService = TokenService.getInstancia();
    }

    /** Los tres reportes en una sola respuesta, o 404 si no hay países. */
    @GET
    public Response generar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
        if (tokenService.validarToken(authorization) == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(RespuestaDTO.error(RespuestaDTO.TOKEN_INVALIDO)).build();
        }
        try {
            return Response.ok(RespuestaDTO.ok("Reportes generados correctamente", controlador.generarReportes()))
                    .build();
        } catch (Exception e) {
            if (RespuestaDTO.esErrorInterno(e)) {
                LOG.log(Level.SEVERE, "Error en /Reportes", e);
            }
            return Response.status(RespuestaDTO.codigoHttp(e)).entity(RespuestaDTO.deExcepcion(e)).build();
        }
    }
}
