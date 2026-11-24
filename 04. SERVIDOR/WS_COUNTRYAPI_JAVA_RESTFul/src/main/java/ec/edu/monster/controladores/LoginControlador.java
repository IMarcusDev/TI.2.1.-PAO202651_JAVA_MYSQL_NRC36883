package ec.edu.monster.controladores;

import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.enums.EstadoUsuario;
import ec.edu.monster.modelos.interfaces.IUsuarioDAO;
import ec.edu.monster.modelos.repositorios.UsuarioDAO;
import ec.edu.monster.servicios.HashService;
import ec.edu.monster.servicios.TokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Inicio y cierre de sesión (RQF-001, ECUD1).
 *
 * @author Escobar Marcos
 */
public class LoginControlador {

    static final String CREDENCIALES_INCORRECTAS = "Credenciales incorrectas";

    private final IUsuarioDAO usuarioDAO;
    private final HashService hashService;
    private final TokenService tokenService;

    public LoginControlador() {
        this(new UsuarioDAO(), new HashService(), TokenService.getInstancia());
    }

    public LoginControlador(IUsuarioDAO usuarioDAO, HashService hashService, TokenService tokenService) {
        this.usuarioDAO = usuarioDAO;
        this.hashService = hashService;
        this.tokenService = tokenService;
    }

    /**
     * Busca el usuario, compara el hash SHA-256 de la clave con el registrado y verifica que
     * esté ACTIVO. Si todo es correcto registra un token nuevo.
     *
     * @throws SecurityException «Credenciales incorrectas» (401), sin decir qué dato falló
     * @throws SQLException si no hay conexión con la base (500)
     */
    public Sesion autenticar(String usuario, String clave) throws SQLException {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            throw new SecurityException(CREDENCIALES_INCORRECTAS);
        }
        Usuario encontrado = usuarioDAO.buscarPorUsername(usuario.trim());
        if (encontrado == null
                || encontrado.getEstado() != EstadoUsuario.ACTIVO
                || !mismoHash(hashService.sha256(clave), encontrado.getPasswordHash())) {
            throw new SecurityException(CREDENCIALES_INCORRECTAS);
        }
        String token = tokenService.generarToken(encontrado);
        return new Sesion(token, encontrado, LocalDateTime.now());
    }

    /** Elimina el token de la sesión. */
    public void cerrarSesion(String token) {
        tokenService.eliminarToken(token);
    }

    /** Compara los dos hash en tiempo constante, sin distinguir mayúsculas. */
    private static boolean mismoHash(String calculado, String registrado) {
        if (registrado == null) {
            return false;
        }
        return MessageDigest.isEqual(
                calculado.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII),
                registrado.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }
}
