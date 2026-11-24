package ec.edu.monster.dobles;

import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.LoginRequest;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.Reporte;
import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.modelos.Universidad;
import ec.edu.monster.vista.BusquedaVista;
import ec.edu.monster.vista.ConsultaApiVista;
import ec.edu.monster.vista.ImportacionVista;
import ec.edu.monster.vista.LoginVista;
import ec.edu.monster.vista.MenuVista;
import ec.edu.monster.vista.PaisVista;
import ec.edu.monster.vista.ReporteVista;
import ec.edu.monster.vista.UniversidadVista;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Vista en memoria que implementa todas las interfaces de vista: guarda lo que el controlador
 * pidió mostrar y devuelve lo que la prueba programó como «entrada del usuario».
 */
public class VistaFalsa implements LoginVista, MenuVista, PaisVista, BusquedaVista, ConsultaApiVista,
        ImportacionVista, ReporteVista, UniversidadVista {

    // ---- lo que el controlador mostró
    public final List<String> mensajes = new ArrayList<>();
    public final List<String> errores = new ArrayList<>();
    public int vecesInicioSesion;
    public int vecesFormularioLogin;
    public boolean claveLimpiada;
    public String menuMostradoPara;
    public List<Pais> tabla;
    public List<String> filtrosRegion;
    public List<Pais> resultadosBusqueda;
    public int totalBusqueda = -1;
    public List<Pais> resultadosConsulta;
    public ResumenImportacion resumen;
    public Reporte reporte;
    public List<Universidad> universidades;
    /** Datos con los que se abrió cada formulario de país. */
    public final List<Pais> formulariosAbiertos = new ArrayList<>();
    public Pais confirmacionPedidaPara;

    // ---- lo que «escribe» el usuario
    public String usuario = "";
    public String clave = "";
    public String filtro = "";
    /** Respuestas sucesivas del formulario de país; null significa «Cancelar». */
    public final Deque<Pais> respuestasFormulario = new LinkedList<>();
    public Pais seleccionado;
    public boolean confirmar = true;
    public CriteriosBusqueda criterios = new CriteriosBusqueda();
    public String nombreConsulta = "";
    public Pais seleccionConsulta;
    public boolean confirmarImportacion = true;
    public String paisUniversidades = "";

    public String ultimoError() {
        return errores.isEmpty() ? null : errores.get(errores.size() - 1);
    }

    public String ultimoMensaje() {
        return mensajes.isEmpty() ? null : mensajes.get(mensajes.size() - 1);
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        mensajes.add(mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        errores.add(mensaje);
    }

    @Override
    public void irAInicioSesion() {
        vecesInicioSesion++;
    }

    @Override
    public void mostrarFormulario() {
        vecesFormularioLogin++;
    }

    @Override
    public LoginRequest leerCredenciales() {
        return new LoginRequest(usuario, clave);
    }

    @Override
    public void limpiarClave() {
        claveLimpiada = true;
    }

    @Override
    public void mostrarMenu(String nombre) {
        menuMostradoPara = nombre;
    }

    @Override
    public int leerOpcion() {
        return 0;
    }

    @Override
    public void mostrarTabla(List<Pais> paises) {
        tabla = paises;
    }

    @Override
    public String leerFiltro() {
        return filtro;
    }

    @Override
    public Pais leerPais(Pais inicial) {
        formulariosAbiertos.add(inicial);
        return respuestasFormulario.isEmpty() ? null : respuestasFormulario.poll();
    }

    @Override
    public Pais paisSeleccionado() {
        return seleccionado;
    }

    @Override
    public boolean confirmarEliminacion(Pais pais) {
        confirmacionPedidaPara = pais;
        return confirmar;
    }

    @Override
    public CriteriosBusqueda leerCriterios() {
        return criterios;
    }

    @Override
    public void mostrarFiltrosRegion(List<String> regiones) {
        filtrosRegion = regiones;
    }

    @Override
    public void mostrarResultados(List<Pais> paises, int total) {
        resultadosBusqueda = paises;
        totalBusqueda = total;
    }

    @Override
    public String leerNombre() {
        return nombreConsulta;
    }

    @Override
    public void mostrarResultados(List<Pais> paises) {
        resultadosConsulta = paises;
    }

    @Override
    public Pais leerSeleccion() {
        return seleccionConsulta;
    }

    @Override
    public boolean confirmarImportacion() {
        return confirmarImportacion;
    }

    @Override
    public void mostrarResumen(ResumenImportacion resumen) {
        this.resumen = resumen;
    }

    @Override
    public void mostrarReporte(Reporte reporte) {
        this.reporte = reporte;
    }

    @Override
    public String leerPais() {
        return paisUniversidades;
    }

    @Override
    public void mostrarUniversidades(List<Universidad> universidades) {
        this.universidades = universidades;
    }
}
