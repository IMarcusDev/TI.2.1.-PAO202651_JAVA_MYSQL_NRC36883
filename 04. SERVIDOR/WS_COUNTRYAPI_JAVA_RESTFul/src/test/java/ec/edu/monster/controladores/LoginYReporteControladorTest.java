package ec.edu.monster.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.dto.ReporteDTO;
import ec.edu.monster.modelos.enums.EstadoUsuario;
import ec.edu.monster.servicios.HashService;
import ec.edu.monster.servicios.TokenService;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Inicio de sesión (ECUD1) y reportes (ECUD6) con DAO en memoria.
 */
class LoginYReporteControladorTest {

    private final HashService hash = new HashService();
    private final Map<String, Usuario> usuarios = Map.of(
            "monster", new Usuario(1, "monster", hash.sha256("monster9"), "Marcos Escobar", EstadoUsuario.ACTIVO),
            "baja", new Usuario(2, "baja", hash.sha256("clave"), "Usuario Inactivo", EstadoUsuario.INACTIVO));
    private final LoginControlador login = new LoginControlador(usuarios::get, hash, TokenService.getInstancia());

    @Test
    void credencialesCorrectasEntreganUnTokenValido() throws SQLException {
        Sesion sesion = login.autenticar("monster", "monster9");
        assertNotNull(sesion.getToken());
        assertEquals("Marcos Escobar", sesion.getUsuario().getNombre());
        assertNotNull(TokenService.getInstancia().validarToken("Bearer " + sesion.getToken()));
    }

    @Test
    void cualquierFallaDeCredencialesDaElMismoMensaje() {
        String[][] casos = {{"monster", "otra"}, {"nadie", "monster9"}, {"baja", "clave"}, {"", ""}, {null, null}};
        for (String[] caso : casos) {
            SecurityException e = assertThrows(SecurityException.class, () -> login.autenticar(caso[0], caso[1]));
            assertEquals("Credenciales incorrectas", e.getMessage());
        }
    }

    @Test
    void reportesSinPaisesEsNoEncontrado() {
        ReporteControlador reportes = new ReporteControlador(new DaosFalsos.PaisDAOFalso());
        NoSuchElementException e = assertThrows(NoSuchElementException.class, reportes::generarReportes);
        assertEquals("No hay países registrados para generar reportes", e.getMessage());
    }

    @Test
    void reportesMuestranLasSeisRegionesYElMasPoblado() throws SQLException {
        DaosFalsos.PaisDAOFalso paisDAO = new DaosFalsos.PaisDAOFalso();
        paisDAO.agregar("Ecuador", "América", 17_643_060);
        paisDAO.agregar("China", "Asia", 1_402_112_000);
        ReporteDTO reporte = new ReporteControlador(paisDAO).generarReportes();
        assertEquals(6, reporte.getPaisesPorRegion().size());
        assertEquals("China", reporte.getPaisMasPoblado().getNombre());
    }
}
