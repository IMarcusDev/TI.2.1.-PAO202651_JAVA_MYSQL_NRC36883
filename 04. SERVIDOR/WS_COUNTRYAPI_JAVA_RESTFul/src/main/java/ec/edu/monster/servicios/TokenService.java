package ec.edu.monster.servicios;

import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.data.Usuario;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones de los usuarios, guardadas en memoria (RQF-007, RNF-005).
 *
 * <ul>
 *   <li>El token es un UUID que se entrega al iniciar sesión.</li>
 *   <li>Expira tras {@value #MINUTOS_EXPIRACION} minutos sin uso; cada uso válido renueva ese tiempo.</li>
 *   <li>Llega en el encabezado Authorization, con o sin el prefijo «Bearer ».</li>
 *   <li>Al reiniciar el servidor se pierden todas las sesiones.</li>
 * </ul>
 * Todos los servicios web comparten la misma instancia ({@link #getInstancia()}).
 *
 * @author Escobar Marcos
 */
public class TokenService {

    public static final int MINUTOS_EXPIRACION = 15;

    private static final TokenService INSTANCIA = new TokenService(Clock.systemDefaultZone());

    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private final Clock reloj;

    /** El reloj se puede reemplazar en las pruebas para simular el paso del tiempo. */
    TokenService(Clock reloj) {
        this.reloj = reloj;
    }

    /** Instancia única que comparten todos los servicios web. */
    public static TokenService getInstancia() {
        return INSTANCIA;
    }

    /** Registra una sesión nueva para el usuario y devuelve su token. */
    public String generarToken(Usuario usuario) {
        eliminarExpirados();
        String token = UUID.randomUUID().toString();
        sesiones.put(token, new Sesion(token, usuario, ahora()));
        return token;
    }

    /**
     * Comprueba el token y, si es válido, renueva la hora del último uso.
     *
     * @param token valor del encabezado Authorization, con o sin «Bearer »
     * @return el usuario de la sesión, o null si el token falta, no existe o expiró
     */
    public Usuario validarToken(String token) {
        String limpio = limpiar(token);
        if (limpio == null) {
            return null;
        }
        Sesion sesion = sesiones.get(limpio);
        if (sesion == null) {
            return null;
        }
        LocalDateTime ahora = ahora();
        if (expirada(sesion, ahora)) {
            sesiones.remove(limpio);
            return null;
        }
        sesion.setUltimoUso(ahora);
        return sesion.getUsuario();
    }

    /** Cierra la sesión del token. No falla si el token ya no existe. */
    public void eliminarToken(String token) {
        String limpio = limpiar(token);
        if (limpio != null) {
            sesiones.remove(limpio);
        }
    }

    /** Quita el prefijo «Bearer » (sin distinguir mayúsculas) y los espacios; null si queda vacío. */
    private static String limpiar(String token) {
        if (token == null) {
            return null;
        }
        String valor = token.trim();
        if (valor.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            valor = valor.substring("bearer ".length()).trim();
        }
        return valor.isEmpty() ? null : valor;
    }

    private boolean expirada(Sesion sesion, LocalDateTime ahora) {
        return sesion.getUltimoUso().plusMinutes(MINUTOS_EXPIRACION).isBefore(ahora);
    }

    /** Libera la memoria de las sesiones abandonadas. */
    private void eliminarExpirados() {
        LocalDateTime ahora = ahora();
        sesiones.values().removeIf(s -> expirada(s, ahora));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }
}
