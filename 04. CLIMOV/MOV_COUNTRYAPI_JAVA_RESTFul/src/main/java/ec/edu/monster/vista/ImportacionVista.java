package ec.edu.monster.vista;

import ec.edu.monster.modelos.ResumenImportacion;

/**
 * «Importar países»: confirmación y resumen (ECUD2).
 *
 * @author Escobar Marcos
 */
public interface ImportacionVista extends VistaBase {

    /** «¿Importar los países desde la API REST Countries? Los países existentes se actualizarán». */
    boolean confirmarImportacion();

    void mostrarResumen(ResumenImportacion resumen);
}
