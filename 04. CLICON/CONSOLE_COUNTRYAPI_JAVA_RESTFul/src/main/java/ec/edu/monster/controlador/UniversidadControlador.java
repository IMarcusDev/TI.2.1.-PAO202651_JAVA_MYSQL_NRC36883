package ec.edu.monster.controlador;

import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.modelos.Universidad;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.servicios.UniversidadServicio;
import ec.edu.monster.vista.UniversidadVista;
import java.util.List;

/**
 * Importar y consultar universidades por país (ECUD10).
 *
 * @author Escobar Marcos
 */
public class UniversidadControlador {

    static final String SIN_UNIVERSIDADES = "No hay universidades guardadas para ese país";

    private final UniversidadVista vista;
    private final UniversidadServicio servicio;
    private final SesionServicio sesion;

    public UniversidadControlador(UniversidadVista vista, UniversidadServicio servicio, SesionServicio sesion) {
        this.vista = vista;
        this.servicio = servicio;
        this.sesion = sesion;
    }

    /** Importa las universidades del país escrito, muestra el resumen y recarga la tabla. */
    public void importar() {
        String pais = vista.leerPais();
        String error = ValidadorDatos.validarPaisUniversidades(pais);
        if (error != null) {
            vista.mostrarError(error);
            return;
        }
        RespuestaDTO<ResumenImportacion> respuesta = servicio.importar(sesion.getToken(), pais);
        if (fallo(respuesta)) {
            return;
        }
        cargar(pais, false);
        vista.mostrarResumen(respuesta.getDatos());
    }

    /** Muestra las universidades guardadas del país escrito («Ver guardadas»). */
    public void listar() {
        String pais = vista.leerPais();
        String error = ValidadorDatos.validarPaisUniversidades(pais);
        if (error != null) {
            vista.mostrarError(error);
            return;
        }
        cargar(pais, true);
    }

    private void cargar(String pais, boolean avisarSiVacia) {
        RespuestaDTO<List<Universidad>> respuesta = servicio.listar(sesion.getToken(), pais);
        if (fallo(respuesta)) {
            return;
        }
        vista.mostrarUniversidades(respuesta.getDatos());
        if (avisarSiVacia && respuesta.getDatos().isEmpty()) {
            vista.mostrarMensaje(SIN_UNIVERSIDADES);
        }
    }

    private boolean fallo(RespuestaDTO<?> respuesta) {
        if (respuesta.esOk()) {
            return false;
        }
        vista.mostrarError(respuesta.getMensaje());
        if (respuesta.isSesionExpirada()) {
            sesion.cerrar();
            vista.irAInicioSesion();
        }
        return true;
    }
}
