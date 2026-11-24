package ec.edu.monster.vista;

import ec.edu.monster.modelos.ResumenImportacion;

/**
 * «Importar países» (ECUD2) en importar.jsp. La confirmación es un panel de la página: su
 * botón envía {@code confirmado=si}.
 *
 * @author Escobar Marcos
 */
public class ImportacionVistaWeb extends VistaWeb implements ImportacionVista {

    public static final String RESUMEN = "resumen";

    @Override
    public boolean confirmarImportacion() {
        return "si".equals(parametro("confirmado"));
    }

    @Override
    public void mostrarResumen(ResumenImportacion resumen) {
        poner(RESUMEN, resumen);
        mostrarMensaje("Importación finalizada: " + resumen.getRecibidos() + " países recibidos, "
                + resumen.getRegistrados() + " registrados y " + resumen.getActualizados() + " actualizados");
    }
}
