package ec.edu.monster.ws;

import ec.edu.monster.controladores.PaisControlador;
import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.dto.PaisDTO;
import ec.edu.monster.modelos.dto.RespuestaDTO;
import ec.edu.monster.servicios.TokenService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de países: /api/Paises (ECUD2 a ECUD5). Todas las operaciones exigen un token
 * válido en el encabezado Authorization.
 *
 * @author Escobar Marcos
 */
@Path("/Paises")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
public class PaisWebService {

    private static final Logger LOG = Logger.getLogger(PaisWebService.class.getName());

    private final PaisControlador controlador;
    private final TokenService tokenService;

    public PaisWebService() {
        this.controlador = new PaisControlador();
        this.tokenService = TokenService.getInstancia();
    }

    /** GET /api/Paises[?nombre=texto]: listado ordenado por nombre, o búsqueda por nombre. */
    @GET
    public Response listar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @QueryParam("nombre") String nombre) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            List<Pais> paises = controlador.buscarPorNombre(nombre);
            return Response.ok(RespuestaDTO.ok("Consulta realizada correctamente", aDTO(paises))).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /** POST /api/Paises: registra un país (201) o responde 400 / 409. */
    @POST
    public Response registrar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization, PaisDTO pais) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            Pais registrado = controlador.registrarPais(pais == null ? null : pais.aPais());
            return Response.status(Response.Status.CREATED)
                    .entity(RespuestaDTO.ok("País registrado correctamente", PaisDTO.desde(registrado, true)))
                    .build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /** PUT /api/Paises/{id}: actualiza el país o responde 400 / 404 / 409. */
    @PUT
    @Path("/{id}")
    public Response actualizar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("id") int id, PaisDTO pais) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            Pais actualizado = controlador.actualizarPais(id, pais == null ? null : pais.aPais());
            return Response.ok(RespuestaDTO.ok("País actualizado correctamente", PaisDTO.desde(actualizado, true)))
                    .build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /** DELETE /api/Paises/{id}: elimina el país o responde 404. */
    @DELETE
    @Path("/{id}")
    public Response eliminar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("id") int id) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            controlador.eliminarPais(id);
            return Response.ok(RespuestaDTO.ok("País eliminado correctamente", null)).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /** GET /api/Paises/buscar?nombre=&amp;capital=&amp;region=: criterios combinables. */
    @GET
    @Path("/buscar")
    public Response buscar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @QueryParam("nombre") String nombre, @QueryParam("capital") String capital,
            @QueryParam("region") String region) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            List<Pais> paises = controlador.buscarPaises(nombre, capital, region);
            return Response.ok(RespuestaDTO.ok("Búsqueda realizada correctamente", aDTO(paises))).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /** POST /api/Paises/importar: importa todos los países de REST Countries. */
    @POST
    @Path("/importar")
    public Response importar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            return Response.ok(RespuestaDTO.ok("Importación finalizada", controlador.importarPaises())).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /** GET /api/Paises/externo/{nombre}: consulta el país en REST Countries sin guardarlo. */
    @GET
    @Path("/externo/{nombre}")
    public Response consultarExterno(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("nombre") String nombre) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            return Response.ok(RespuestaDTO.ok("Consulta en la API exitosa", controlador.consultarEnAPI(nombre)))
                    .build();
        } catch (Exception e) {
            return error(e);
        }
    }

    /** POST /api/Paises/guardar: guarda el país elegido de la consulta a la API (registra o actualiza). */
    @POST
    @Path("/guardar")
    public Response guardar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization, PaisDTO pais) {
        if (tokenService.validarToken(authorization) == null) {
            return tokenInvalido();
        }
        try {
            Pais guardado = controlador.guardarDesdeAPI(pais == null ? null : pais.aPais());
            return Response.ok(RespuestaDTO.ok("País guardado correctamente", PaisDTO.desde(guardado, true))).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    private static List<PaisDTO> aDTO(List<Pais> paises) {
        return paises.stream().map(p -> PaisDTO.desde(p, true)).toList();
    }

    private static Response tokenInvalido() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(RespuestaDTO.error(RespuestaDTO.TOKEN_INVALIDO)).build();
    }

    private static Response error(Exception e) {
        if (RespuestaDTO.esErrorInterno(e)) {
            LOG.log(Level.SEVERE, "Error en /Paises", e);
        }
        return Response.status(RespuestaDTO.codigoHttp(e)).entity(RespuestaDTO.deExcepcion(e)).build();
    }
}
