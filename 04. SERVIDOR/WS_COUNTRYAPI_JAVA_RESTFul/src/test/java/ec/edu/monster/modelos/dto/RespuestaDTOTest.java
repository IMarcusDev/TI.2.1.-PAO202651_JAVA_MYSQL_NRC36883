package ec.edu.monster.modelos.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.modelos.enums.EstadoRespuesta;
import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Código HTTP y mensaje de cada tipo de error (decisión A2 de DUDAS.md).
 */
class RespuestaDTOTest {

    @Test
    void cadaExcepcionTieneSuCodigoHttp() {
        assertEquals(400, RespuestaDTO.codigoHttp(new IllegalArgumentException("x")));
        assertEquals(401, RespuestaDTO.codigoHttp(new SecurityException("x")));
        assertEquals(404, RespuestaDTO.codigoHttp(new NoSuchElementException("x")));
        assertEquals(409, RespuestaDTO.codigoHttp(new IllegalStateException("x")));
        assertEquals(502, RespuestaDTO.codigoHttp(new IOException("x")));
        assertEquals(500, RespuestaDTO.codigoHttp(new SQLException("x")));
        assertEquals(500, RespuestaDTO.codigoHttp(new NullPointerException()));
    }

    @Test
    void losErroresDeNegocioConservanSuMensaje() {
        RespuestaDTO<Object> r = RespuestaDTO.deExcepcion(new IllegalStateException("Ya existe un país con ese nombre"));
        assertEquals(EstadoRespuesta.ERROR, r.getEstado());
        assertEquals("Ya existe un país con ese nombre", r.getMensaje());
        assertNull(r.getDatos());
    }

    @Test
    void losErroresDeBaseDeDatosNoExponenElSql() {
        RespuestaDTO<Object> r = RespuestaDTO.deExcepcion(
                new SQLException("Communications link failure: SELECT * FROM pais"));
        assertEquals("Servicio no disponible, intente más tarde", r.getMensaje());
    }

    @Test
    void losErroresInesperadosUsanUnMensajeFijo() {
        RespuestaDTO<Object> r = RespuestaDTO.deExcepcion(new NullPointerException("campo x es null"));
        assertEquals("Ocurrió un error inesperado, intente nuevamente", r.getMensaje());
    }
}
