package ec.edu.monster.controlador;

import ec.edu.monster.modelos.Reporte;
import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.servicios.ReporteServicio;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.vista.ReporteVista;

/**
 * Reportes estadísticos (ECUD6).
 *
 * @author Escobar Marcos
 */
public class ReporteControlador {

    static final String SUGERENCIA_IMPORTAR = ". Use «Importar países» para cargarlos.";

    private final ReporteVista vista;
    private final ReporteServicio servicio;
    private final SesionServicio sesion;

    public ReporteControlador(ReporteVista vista, ReporteServicio servicio, SesionServicio sesion) {
        this.vista = vista;
        this.servicio = servicio;
        this.sesion = sesion;
    }

    /** Pide los tres reportes; si no hay países, sugiere importarlos (ECUD6, flujo alternativo 2). */
    public void generar() {
        RespuestaDTO<Reporte> respuesta = servicio.generar(sesion.getToken());
        if (respuesta.esOk()) {
            vista.mostrarReporte(respuesta.getDatos());
            return;
        }
        if (respuesta.getCodigoHttp() == 404) {
            vista.mostrarError(respuesta.getMensaje() + SUGERENCIA_IMPORTAR);
            return;
        }
        vista.mostrarError(respuesta.getMensaje());
        if (respuesta.isSesionExpirada()) {
            sesion.cerrar();
            vista.irAInicioSesion();
        }
    }
}
