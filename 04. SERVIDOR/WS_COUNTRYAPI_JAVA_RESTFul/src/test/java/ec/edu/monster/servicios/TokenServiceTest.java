package ec.edu.monster.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.enums.EstadoUsuario;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Generación, validación, renovación, expiración y eliminación de tokens.
 */
class TokenServiceTest {

    /** Reloj que avanza solo cuando la prueba lo pide. */
    private static final class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-11-23T10:00:00Z");

        void avanzar(Duration tiempo) {
            ahora = ahora.plus(tiempo);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private RelojManual reloj;
    private TokenService tokenService;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        reloj = new RelojManual();
        tokenService = new TokenService(reloj);
        usuario = new Usuario(1, "monster", "hash", "Marcos Escobar", EstadoUsuario.ACTIVO);
    }

    @Test
    void generaUnUuidYLoValida() {
        String token = tokenService.generarToken(usuario);
        assertNotNull(UUID.fromString(token));
        assertEquals("Marcos Escobar", tokenService.validarToken(token).getNombre());
    }

    @Test
    void aceptaElTokenConOSinPrefijoBearer() {
        String token = tokenService.generarToken(usuario);
        assertNotNull(tokenService.validarToken("Bearer " + token));
        assertNotNull(tokenService.validarToken("bearer " + token));
        assertNotNull(tokenService.validarToken("  " + token + "  "));
    }

    @Test
    void rechazaTokenAusenteVacioODesconocido() {
        assertNull(tokenService.validarToken(null));
        assertNull(tokenService.validarToken(""));
        assertNull(tokenService.validarToken("Bearer "));
        assertNull(tokenService.validarToken(UUID.randomUUID().toString()));
    }

    @Test
    void sigueValidoAlCumplirExactamente15Minutos() {
        String token = tokenService.generarToken(usuario);
        reloj.avanzar(Duration.ofMinutes(15));
        assertNotNull(tokenService.validarToken(token));
    }

    @Test
    void expiraDespuesDe15MinutosSinUsoYSeElimina() {
        String token = tokenService.generarToken(usuario);
        reloj.avanzar(Duration.ofMinutes(15).plusSeconds(1));
        assertNull(tokenService.validarToken(token));
        // Ya se eliminó: aunque el reloj no cambie, sigue siendo inválido.
        assertNull(tokenService.validarToken(token));
    }

    @Test
    void cadaUsoRenuevaElTiempo() {
        String token = tokenService.generarToken(usuario);
        reloj.avanzar(Duration.ofMinutes(10));
        assertNotNull(tokenService.validarToken(token));
        reloj.avanzar(Duration.ofMinutes(10));
        assertNotNull(tokenService.validarToken(token), "20 minutos en total, pero solo 10 sin uso");
    }

    @Test
    void eliminarTokenCierraLaSesion() {
        String token = tokenService.generarToken(usuario);
        tokenService.eliminarToken("Bearer " + token);
        assertNull(tokenService.validarToken(token));
        // Eliminar un token inexistente no falla.
        tokenService.eliminarToken(token);
        tokenService.eliminarToken(null);
    }
}
