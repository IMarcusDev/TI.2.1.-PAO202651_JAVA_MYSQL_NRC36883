package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.modelos.Pais;
import java.util.List;

/**
 * «Consultar país en la API» (ECUD3): busca en REST Countries a través del servidor y guarda el
 * país elegido. Cada resultado indica si ya está guardado.
 *
 * @author Escobar Marcos
 */
public class ConsultaApiFragment extends FragmentoBase implements ConsultaApiVista {

    private final PaisAdaptador adaptador = new PaisAdaptador(true);
    private ApiControlador controlador;
    private EditText nombre;
    private List<Pais> resultados;
    private volatile Pais seleccionado;

    void setControlador(ApiControlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        return inflador.inflate(R.layout.fragment_consulta_api, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        nombre = vista.findViewById(R.id.campoNombreApi);
        RecyclerView lista = vista.findViewById(R.id.listaApi);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adaptador);
        adaptador.setAlTocar((pais, tarjeta) -> elegir(pais));
        adaptador.setAlGuardar(pais -> {
            elegir(pais);
            if (controlador != null) {
                ejecutar(controlador::guardar);
            }
        });

        vista.findViewById(R.id.botonConsultar).setOnClickListener(v -> consultar());
        nombre.setOnEditorActionListener((campo, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                consultar();
                return true;
            }
            return false;
        });
        if (resultados != null) {
            adaptador.setPaises(resultados);
        }
    }

    private void consultar() {
        if (controlador != null) {
            ejecutar(controlador::consultar);
        }
    }

    private void elegir(Pais pais) {
        seleccionado = pais;
        adaptador.seleccionar(pais);
    }

    // ------------------------------------------------------------------ ConsultaApiVista

    @Override
    public String leerNombre() {
        return Hilos.enPrincipalYEsperar(() -> nombre == null ? "" : nombre.getText().toString());
    }

    @Override
    public void mostrarResultados(List<Pais> paises) {
        Hilos.enPrincipal(() -> {
            resultados = paises;
            if (seleccionado != null && (paises == null || !paises.contains(seleccionado))) {
                seleccionado = null;
            }
            adaptador.setPaises(paises);
        });
    }

    @Override
    public Pais leerSeleccion() {
        return seleccionado;
    }
}
