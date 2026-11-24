package ec.edu.monster.vista;

import ec.edu.monster.modelos.Reporte;

/**
 * Reportes (ECUD6) en reportes.jsp.
 *
 * @author Escobar Marcos
 */
public class ReporteVistaWeb extends VistaWeb implements ReporteVista {

    public static final String REPORTE = "reporte";

    @Override
    public void mostrarReporte(Reporte reporte) {
        poner(REPORTE, reporte);
    }
}
