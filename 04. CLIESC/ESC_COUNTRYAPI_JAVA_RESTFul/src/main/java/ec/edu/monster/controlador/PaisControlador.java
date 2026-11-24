package ec.edu.monster.controlador;

import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.servicios.PaisServicio;
import ec.edu.monster.servicios.SesionServicio;
import ec.edu.monster.vista.BusquedaVista;
import ec.edu.monster.vista.PaisVista;
import ec.edu.monster.vista.VistaBase;
import java.util.List;

/**
 * Gestión de países (ECUD4: listar, buscar, registrar, actualizar y eliminar) y «Buscar y
 * filtrar» (ECUD5).
 *
 * @author Escobar Marcos
 */
public class PaisControlador {

    static final String TABLA_VACIA = "No hay países registrados. Use «Importar países» para cargarlos";
    static final String SIN_RESULTADOS = "No se encontraron países";
    static final String SIN_RESULTADOS_CRITERIOS = "No se encontraron países con esos criterios";
    static final String SELECCIONE_PAIS = "Seleccione un país de la tabla";

    private final PaisVista vista;
    private final BusquedaVista vistaBusqueda;
    private final PaisServicio servicio;
    private final SesionServicio sesion;

    public PaisControlador(PaisVista vista, BusquedaVista vistaBusqueda, PaisServicio servicio, SesionServicio sesion) {
        this.vista = vista;
        this.vistaBusqueda = vistaBusqueda;
        this.servicio = servicio;
        this.sesion = sesion;
        vistaBusqueda.mostrarFiltrosRegion(Pais.REGIONES);
    }

    /** Muestra todos los países ordenados por nombre. */
    public void listar() {
        RespuestaDTO<List<Pais>> respuesta = servicio.listar(sesion.getToken(), null);
        if (fallo(respuesta, vista)) {
            return;
        }
        vista.mostrarTabla(respuesta.getDatos());
        if (respuesta.getDatos().isEmpty()) {
            vista.mostrarMensaje(TABLA_VACIA);
        }
    }

    /** Filtra la tabla por el texto del campo «Buscar» (coincidencia parcial en el nombre). */
    public void buscar() {
        String texto = vista.leerFiltro();
        RespuestaDTO<List<Pais>> respuesta = servicio.listar(sesion.getToken(), texto);
        if (fallo(respuesta, vista)) {
            return;
        }
        vista.mostrarTabla(respuesta.getDatos());
        if (respuesta.getDatos().isEmpty()) {
            vista.mostrarMensaje(texto == null || texto.isBlank() ? TABLA_VACIA : SIN_RESULTADOS);
        }
    }

    /** Pide los datos de un país nuevo y lo registra. */
    public void registrar() {
        guardarFormulario(null);
    }

    /** Edita el país elegido en la tabla. */
    public void actualizar() {
        Pais seleccionado = vista.paisSeleccionado();
        if (seleccionado == null) {
            vista.mostrarError(SELECCIONE_PAIS);
            return;
        }
        guardarFormulario(seleccionado.copia());
    }

    /** Elimina el país elegido después de pedir confirmación. */
    public void eliminar() {
        Pais seleccionado = vista.paisSeleccionado();
        if (seleccionado == null) {
            vista.mostrarError(SELECCIONE_PAIS);
            return;
        }
        if (!vista.confirmarEliminacion(seleccionado)) {
            return;
        }
        RespuestaDTO<Void> respuesta = servicio.eliminar(sesion.getToken(), seleccionado.getId());
        if (fallo(respuesta, vista)) {
            if (respuesta.getCodigoHttp() == 404) {
                // Otro usuario ya lo eliminó: se recarga la tabla y el mensaje queda visible.
                recargarSinMensajes();
            }
            return;
        }
        recargarSinMensajes();
        vista.mostrarMensaje(respuesta.getMensaje());
    }

    /** Busca con los criterios de «Buscar y filtrar»; también se llama al cambiar la región. */
    public void buscarYFiltrar() {
        CriteriosBusqueda criterios = vistaBusqueda.leerCriterios();
        RespuestaDTO<List<Pais>> respuesta = servicio.buscar(sesion.getToken(), criterios);
        if (fallo(respuesta, vistaBusqueda)) {
            return;
        }
        List<Pais> paises = respuesta.getDatos();
        vistaBusqueda.mostrarResultados(paises, paises.size());
        if (paises.isEmpty()) {
            vistaBusqueda.mostrarMensaje(SIN_RESULTADOS_CRITERIOS);
        }
    }

    /**
     * Abre el formulario hasta que el usuario cancele o el servidor acepte los datos. Si el
     * cliente o el servidor rechazan los datos (400 o 409), muestra el mensaje y vuelve a abrir
     * el formulario con lo que el usuario había escrito.
     */
    private void guardarFormulario(Pais inicial) {
        boolean nuevo = inicial == null;
        int id = nuevo ? 0 : inicial.getId();
        Pais datos = inicial;
        while (true) {
            datos = vista.leerPais(datos);
            if (datos == null) {
                return;
            }
            datos.setId(id);
            String error = ValidadorDatos.validarPais(datos);
            if (error != null) {
                vista.mostrarError(error);
                continue;
            }
            RespuestaDTO<Pais> respuesta = nuevo
                    ? servicio.registrar(sesion.getToken(), datos)
                    : servicio.actualizar(sesion.getToken(), id, datos);
            if (!fallo(respuesta, vista)) {
                recargarSinMensajes();
                vista.mostrarMensaje(respuesta.getMensaje());
                return;
            }
            int codigo = respuesta.getCodigoHttp();
            if (codigo != 400 && codigo != 409) {
                if (codigo == 404) {
                    recargarSinMensajes();
                }
                return;
            }
        }
    }

    /** Vuelve a cargar la tabla completa sin reemplazar el mensaje que se está mostrando. */
    private void recargarSinMensajes() {
        RespuestaDTO<List<Pais>> respuesta = servicio.listar(sesion.getToken(), null);
        if (respuesta.esOk()) {
            vista.mostrarTabla(respuesta.getDatos());
        }
    }

    /**
     * Si la respuesta es un error lo muestra; si además la sesión expiró, la cierra y vuelve al
     * inicio de sesión.
     *
     * @return true si hubo error
     */
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
