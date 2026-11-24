package ec.edu.monster.controlador;

import ec.edu.monster.modelos.LoginRequest;
import ec.edu.monster.modelos.LoginResponse;
import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.servicios.LoginServicio;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.vista.LoginVista;
import ec.edu.monster.vista.MenuVista;

/**
 * Inicio y cierre de sesión (ECUD1). También abre el menú principal, porque el menú depende de
 * la sesión (MenuVista → LoginControlador en DC_Clientes).
 *
 * @author Escobar Marcos
 */
public class LoginControlador {

    private final LoginVista vista;
    private final MenuVista menu;
    private final LoginServicio servicio;
    private final SesionServicio sesion;

    public LoginControlador(LoginVista vista, MenuVista menu, LoginServicio servicio, SesionServicio sesion) {
        this.vista = vista;
        this.menu = menu;
        this.servicio = servicio;
        this.sesion = sesion;
    }

    /**
     * Valida que haya usuario y clave, pide el token al servidor y, si todo está bien, guarda la
     * sesión y muestra el menú. Con credenciales incorrectas borra la clave; sin conexión
     * conserva lo escrito.
     */
    public void iniciarSesion() {
        LoginRequest credenciales = vista.leerCredenciales();
        String error = ValidadorDatos.validarCredenciales(credenciales.getUsuario(), credenciales.getClave());
        if (error != null) {
            vista.mostrarError(error);
            return;
        }
        RespuestaDTO<LoginResponse> respuesta =
                servicio.login(credenciales.getUsuario().trim(), credenciales.getClave());
        if (!respuesta.esOk() || respuesta.getDatos() == null) {
            vista.mostrarError(respuesta.getMensaje());
            if (respuesta.getCodigoHttp() == 401) {
                vista.limpiarClave();
            }
            return;
        }
        sesion.guardar(respuesta.getDatos().getToken(), respuesta.getDatos().getNombre());
        menu.mostrarMenu(respuesta.getDatos().getNombre());
    }

    /**
     * Avisa al servidor para que elimine el token y vuelve al inicio de sesión, aunque el
     * servidor no responda (ECUD1, flujo alternativo 4).
     */
    public void cerrarSesion() {
        if (sesion.estaActiva()) {
            servicio.logout(sesion.getToken());
        }
        sesion.cerrar();
        menu.irAInicioSesion();
        vista.mostrarFormulario();
    }
}
