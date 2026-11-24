package ec.edu.monster.vista;

import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import java.util.List;

/**
 * «Buscar y filtrar»: nombre, capital y filtros de región que se aplican al momento (ECUD5).
 *
 * @author Escobar Marcos
 */
public interface BusquedaVista extends VistaBase {

    CriteriosBusqueda leerCriterios();

    /** Muestra los filtros de región: «Todas» más las regiones recibidas. */
    void mostrarFiltrosRegion(List<String> regiones);

    void mostrarResultados(List<Pais> paises, int total);
}
