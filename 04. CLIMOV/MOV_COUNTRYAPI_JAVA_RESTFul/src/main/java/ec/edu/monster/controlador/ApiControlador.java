package ec.edu.monster.controlador;

import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.servicios.PaisServicio;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.vista.ConsultaApiVista;
import ec.edu.monster.vista.ImportacionVista;
import ec.edu.monster.vista.VistaBase;
import java.util.List;

/**
 * Operaciones que pasan por la API REST Countries a través del servidor: importar todos los
 * países (ECUD2) y consultar un país por nombre para guardarlo (ECUD3).
 *
 * @author Escobar Marcos
 */
public class ApiControlador {

    static final String SELECCIONE_PAIS = "Seleccione un país de la tabla";

    private final ConsultaApiVista vistaConsulta;
    private final ImportacionVista vistaImportacion;
    private final PaisServicio servicio;
    private final SesionServicio sesion;
    /** Última lista consultada, para marcar el país guardado sin volver a consultar. */
    private List<Pais> resultados = List.of();

    public ApiControlador(ConsultaApiVista vistaConsulta, ImportacionVista vistaImportacion,
            PaisServicio servicio, SesionServicio sesion) {
        this.vistaConsulta = vistaConsulta;
        this.vistaImportacion = vistaImportacion;
        this.servicio = servicio;
        this.sesion = sesion;
    }

    /** Pide confirmación e importa todos los países; muestra el resumen. */
    public void importar() {
        if (!vistaImportacion.confirmarImportacion()) {
            return;
        }
        RespuestaDTO<ResumenImportacion> respuesta = servicio.importar(sesion.getToken());
        if (fallo(respuesta, vistaImportacion)) {
            return;
        }
        vistaImportacion.mostrarResumen(respuesta.getDatos());
    }

    /** Consulta en la API los países que coinciden con el nombre (al menos dos caracteres). */
    public void consultar() {
        String nombre = vistaConsulta.leerNombre();
        String error = ValidadorDatos.validarNombreConsulta(nombre);
        if (error != null) {
            vistaConsulta.mostrarError(error);
            return;
        }
        RespuestaDTO<List<Pais>> respuesta = servicio.consultarExterno(sesion.getToken(), nombre);
        if (fallo(respuesta, vistaConsulta)) {
            return;
        }
        resultados = respuesta.getDatos();
        vistaConsulta.mostrarResultados(resultados);
    }

    /** Guarda en la base el país elegido de los resultados (lo registra o lo actualiza). */
    public void guardar() {
        Pais seleccionado = vistaConsulta.leerSeleccion();
        if (seleccionado == null) {
            vistaConsulta.mostrarError(SELECCIONE_PAIS);
            return;
        }
        RespuestaDTO<Pais> respuesta = servicio.guardar(sesion.getToken(), seleccionado);
        if (fallo(respuesta, vistaConsulta)) {
            return;
        }
        seleccionado.setGuardado(true);
        vistaConsulta.mostrarResultados(resultados);
        vistaConsulta.mostrarMensaje(respuesta.getMensaje());
    }

    private boolean fallo(RespuestaDTO<?> respuesta, VistaBase vistaActual) {
        if (respuesta.esOk()) {
            return false;
        }
        vistaActual.mostrarError(respuesta.getMensaje());
        if (respuesta.isSesionExpirada()) {
            sesion.cerrar();
            vistaActual.irAInicioSesion();
        }
        return true;
    }
}
