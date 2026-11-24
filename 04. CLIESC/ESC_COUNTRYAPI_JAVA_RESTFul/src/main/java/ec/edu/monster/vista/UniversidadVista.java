package ec.edu.monster.vista;

import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.modelos.Universidad;
import java.util.List;

/**
 * Universidades: país en inglés, botón Importar y tabla de las guardadas (ECUD10).
 *
 * @author Escobar Marcos
 */
public interface UniversidadVista extends VistaBase {

    /** País escrito (en inglés, como lo espera la API). */
    String leerPais();

    void mostrarResumen(ResumenImportacion resumen);

    void mostrarUniversidades(List<Universidad> universidades);
}
