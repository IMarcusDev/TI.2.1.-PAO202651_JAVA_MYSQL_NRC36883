package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import ec.edu.monster.R;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import java.util.ArrayList;
import java.util.List;

/**
 * «Buscar y filtrar» (ECUD5): nombre y capital parciales y un filtro de región. Elegir una
 * región vuelve a consultar al momento.
 *
 * @author Escobar Marcos
 */
public class BusquedaFragment extends FragmentoBase implements BusquedaVista {

    private final PaisAdaptador adaptador = new PaisAdaptador(false);
    private PaisControlador controlador;
    private EditText nombre;
    private EditText capital;
    private ChipGroup filtros;
    private TextView total;
    private List<String> regiones = new ArrayList<>();
    /** Región elegida, o null para «Todas». */
    private String regionElegida;
    private List<Pais> resultados;
    private int cantidad;

    void setControlador(PaisControlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        return inflador.inflate(R.layout.fragment_busqueda, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        nombre = vista.findViewById(R.id.campoNombreBusqueda);
        capital = vista.findViewById(R.id.campoCapitalBusqueda);
        filtros = vista.findViewById(R.id.filtrosRegion);
        total = vista.findViewById(R.id.totalBusqueda);
        RecyclerView lista = vista.findViewById(R.id.listaBusqueda);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adaptador);

        vista.findViewById(R.id.botonBuscarFiltrar).setOnClickListener(v -> buscar());
        capital.setOnEditorActionListener((campo, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                buscar();
                return true;
            }
            return false;
        });
        pintarFiltros();
        pintarResultados();
    }

    private void buscar() {
        if (controlador != null) {
            ejecutar(controlador::buscarYFiltrar);
        }
    }

    /** «Todas» más una opción por región; se vuelve a consultar al elegir otra. */
    private void pintarFiltros() {
        if (filtros == null) {
            return;
        }
        filtros.setOnCheckedStateChangeListener(null);
        filtros.removeAllViews();
        filtros.addView(chip(getString(R.string.todas), null));
        for (String region : regiones) {
            filtros.addView(chip(region, region));
        }
        filtros.setOnCheckedStateChangeListener((grupo, elegidos) -> {
            if (elegidos.isEmpty()) {
                return;
            }
            Chip chip = grupo.findViewById(elegidos.get(0));
            regionElegida = (String) chip.getTag();
            buscar();
        });
    }

    private Chip chip(String texto, String region) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_region, filtros, false);
        chip.setId(View.generateViewId());
        chip.setText(texto);
        chip.setTag(region);
        chip.setChecked(region == null ? regionElegida == null : region.equals(regionElegida));
        return chip;
    }

    private void pintarResultados() {
        if (total == null || resultados == null) {
            return;
        }
        adaptador.setPaises(resultados);
        total.setText(getResources().getQuantityString(R.plurals.total_encontrados, cantidad, cantidad));
    }

    // ------------------------------------------------------------------ BusquedaVista

    @Override
    public CriteriosBusqueda leerCriterios() {
        return Hilos.enPrincipalYEsperar(() -> new CriteriosBusqueda(
                Formato.vacioANulo(nombre.getText()),
                Formato.vacioANulo(capital.getText()),
                regionElegida));
    }

    @Override
    public void mostrarFiltrosRegion(List<String> regiones) {
        Hilos.enPrincipal(() -> {
            this.regiones = new ArrayList<>(regiones);
            pintarFiltros();
        });
    }

    @Override
    public void mostrarResultados(List<Pais> paises, int total) {
        Hilos.enPrincipal(() -> {
            resultados = paises;
            cantidad = total;
            pintarResultados();
        });
    }
}
