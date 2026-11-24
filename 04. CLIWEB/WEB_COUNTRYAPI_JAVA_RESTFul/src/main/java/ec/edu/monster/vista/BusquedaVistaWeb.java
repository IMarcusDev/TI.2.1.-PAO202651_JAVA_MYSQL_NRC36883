package ec.edu.monster.vista;

import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import java.util.ArrayList;
import java.util.List;

/**
 * «Buscar y filtrar» (ECUD5) en buscar.jsp: nombre y capital parciales y una región. Al
 * elegir otra región el formulario se envía solo y se vuelve a consultar.
 *
 * @author Escobar Marcos
 */
public class BusquedaVistaWeb extends VistaWeb implements BusquedaVista {

    public static final String RESULTADOS = "resultados";
    public static final String TOTAL = "total";

    private List<String> regiones = new ArrayList<>();

    /** Regiones para los filtros (las fija el controlador al crearse). */
    public List<String> getRegiones() {
        return regiones;
    }

    @Override
    public CriteriosBusqueda leerCriterios() {
        return new CriteriosBusqueda(parametroConValor("nombre"), parametroConValor("capital"),
                parametroConValor("region"));
    }

    @Override
    public void mostrarFiltrosRegion(List<String> regiones) {
        this.regiones = new ArrayList<>(regiones);
    }

    @Override
    public void mostrarResultados(List<Pais> paises, int total) {
        poner(RESULTADOS, paises);
        poner(TOTAL, total);
    }
}
