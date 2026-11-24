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
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.R;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.modelos.Pais;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de países (ECUD4): lista con búsqueda por nombre, «Nuevo» y, al tocar un país,
 * «Editar» o «Eliminar».
 *
 * @author Escobar Marcos
 */
public class PaisFragment extends FragmentoBase implements PaisVista {

    private static final int MENU_EDITAR = 1;
    private static final int MENU_ELIMINAR = 2;

    private final PaisAdaptador adaptador = new PaisAdaptador(false);
    private PaisControlador controlador;
    private EditText filtro;
    private TextView total;
    private List<Pais> paises = new ArrayList<>();
    private volatile Pais seleccionado;
    /** Error que el controlador mostró antes de reabrir el formulario. */
    private volatile String errorFormulario;

    void setControlador(PaisControlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        return inflador.inflate(R.layout.fragment_paises, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        filtro = vista.findViewById(R.id.campoFiltro);
        total = vista.findViewById(R.id.totalPaises);
        RecyclerView lista = vista.findViewById(R.id.listaPaises);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adaptador);
        adaptador.setAlTocar(this::opcionesDe);

        vista.findViewById(R.id.botonBuscar).setOnClickListener(v -> accion(() -> controlador.buscar()));
        filtro.setOnEditorActionListener((campo, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                accion(() -> controlador.buscar());
                return true;
            }
            return false;
        });
        vista.findViewById(R.id.botonNuevo).setOnClickListener(v -> accion(() -> controlador.registrar()));
        pintarTabla();
    }

    /** Al tocar un país se elige y se ofrecen Editar y Eliminar. */
    private void opcionesDe(Pais pais, View tarjeta) {
        seleccionado = pais;
        adaptador.seleccionar(pais);
        PopupMenu menu = new PopupMenu(requireContext(), tarjeta);
        menu.getMenu().add(0, MENU_EDITAR, 0, R.string.editar);
        menu.getMenu().add(0, MENU_ELIMINAR, 1, R.string.eliminar);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == MENU_EDITAR) {
                accion(() -> controlador.actualizar());
            } else {
                accion(() -> controlador.eliminar());
            }
            return true;
        });
        menu.show();
    }

    private void accion(Runnable tarea) {
        if (controlador != null) {
            errorFormulario = null;
            ejecutar(tarea);
        }
    }

    private void pintarTabla() {
        if (total == null) {
            return;
        }
        adaptador.setPaises(paises);
        total.setText(getResources().getQuantityString(R.plurals.total_paises, paises.size(), paises.size()));
    }

    // ------------------------------------------------------------------ PaisVista

    @Override
    public void mostrarError(String mensaje) {
        errorFormulario = mensaje;
        super.mostrarError(mensaje);
    }

    @Override
    public void mostrarTabla(List<Pais> lista) {
        Hilos.enPrincipal(() -> {
            paises = lista == null ? new ArrayList<>() : lista;
            seleccionado = null;
            adaptador.seleccionar(null);
            pintarTabla();
        });
    }

    @Override
    public String leerFiltro() {
        return Hilos.enPrincipalYEsperar(() -> filtro == null ? "" : filtro.getText().toString());
    }

    @Override
    public Pais leerPais(Pais inicial) {
        String error = errorFormulario;
        errorFormulario = null;
        boolean nuevo = inicial == null || inicial.getId() == 0;
        return esperarUsuario(respuesta -> FormularioPais.mostrar(requireContext(), inicial, nuevo, error, respuesta));
    }

    @Override
    public Pais paisSeleccionado() {
        return seleccionado;
    }

    @Override
    public boolean confirmarEliminacion(Pais pais) {
        Boolean confirmado = esperarUsuario(respuesta -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar_pais)
                .setMessage(getString(R.string.eliminar_pregunta, pais.getNombre()))
                .setPositiveButton(R.string.eliminar, (d, b) -> respuesta.accept(Boolean.TRUE))
                .setNegativeButton(R.string.cancelar, (d, b) -> respuesta.accept(Boolean.FALSE))
                .setOnDismissListener(d -> respuesta.accept(Boolean.FALSE))
                .show());
        return Boolean.TRUE.equals(confirmado);
    }
}
