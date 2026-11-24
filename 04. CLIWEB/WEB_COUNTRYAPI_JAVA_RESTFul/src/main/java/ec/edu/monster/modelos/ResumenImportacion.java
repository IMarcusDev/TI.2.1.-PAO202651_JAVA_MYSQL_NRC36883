package ec.edu.monster.modelos;

/**
 * Totales de una importación: recibidos de la API, registrados y actualizados.
 *
 * @author Escobar Marcos
 */
public class ResumenImportacion {

    private int recibidos;
    private int registrados;
    private int actualizados;

    public ResumenImportacion() {
    }

    public ResumenImportacion(int recibidos, int registrados, int actualizados) {
        this.recibidos = recibidos;
        this.registrados = registrados;
        this.actualizados = actualizados;
    }

    public int getRecibidos() {
        return recibidos;
    }

    public int getRegistrados() {
        return registrados;
    }

    public int getActualizados() {
        return actualizados;
    }
}
