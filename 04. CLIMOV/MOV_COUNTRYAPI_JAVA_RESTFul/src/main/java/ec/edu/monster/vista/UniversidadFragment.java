package ec.edu.monster.vista;

import android.annotation.SuppressLint;
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
import ec.edu.monster.R;
import ec.edu.monster.controlador.UniversidadControlador;
import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.modelos.Universidad;
import java.util.ArrayList;
import java.util.List;

/**
 * Universidades (ECUD10): importa desde la API de universidades las de un país (en inglés) y
 * muestra las guardadas.
 *
 * @author Escobar Marcos
 */
public class UniversidadFragment extends FragmentoBase implements UniversidadVista {

    private final Adaptador adaptador = new Adaptador();
    private UniversidadControlador controlador;
    private EditText pais;
    private TextView total;
    private List<Universidad> universidades;

    void setControlador(UniversidadControlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        return inflador.inflate(R.layout.fragment_universidades, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        pais = vista.findViewById(R.id.campoPaisUniversidades);
        total = vista.findViewById(R.id.totalUniversidades);
        RecyclerView lista = vista.findViewById(R.id.listaUniversidades);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adaptador);

        vista.findViewById(R.id.botonImportarUniversidades).setOnClickListener(v -> {
            if (controlador != null) {
                ejecutar(controlador::importar);
            }
        });
        vista.findViewById(R.id.botonVerGuardadas).setOnClickListener(v -> listar());
        pais.setOnEditorActionListener((campo, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                listar();
                return true;
            }
            return false;
        });
        pintar();
    }

    private void listar() {
        if (controlador != null) {
            ejecutar(controlador::listar);
        }
    }

    private void pintar() {
        if (total == null || universidades == null) {
            return;
        }
        adaptador.setUniversidades(universidades);
        total.setText(getResources().getQuantityString(R.plurals.total_universidades,
                universidades.size(), universidades.size()));
    }

    // ------------------------------------------------------------------ UniversidadVista

    @Override
    public String leerPais() {
        return Hilos.enPrincipalYEsperar(() -> pais == null ? "" : pais.getText().toString());
    }

    @Override
    public void mostrarResumen(ResumenImportacion resumen) {
        Hilos.enPrincipal(() -> mostrarMensaje(getString(R.string.importacion_universidades,
                resumen.getRecibidos(), resumen.getRegistrados(), resumen.getActualizados())));
    }

    @Override
    public void mostrarUniversidades(List<Universidad> lista) {
        Hilos.enPrincipal(() -> {
            universidades = lista == null ? new ArrayList<>() : lista;
            pintar();
        });
    }

    /** Tarjetas con nombre, dominio y sitio web (la tabla de los otros clientes). */
    private static class Adaptador extends RecyclerView.Adapter<Adaptador.Fila> {

        private final List<Universidad> universidades = new ArrayList<>();

        @SuppressLint("NotifyDataSetChanged")
        void setUniversidades(List<Universidad> nuevas) {
            universidades.clear();
            universidades.addAll(nuevas);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
            return new Fila(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_universidad, padre, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Fila fila, int posicion) {
            Universidad universidad = universidades.get(posicion);
            fila.nombre.setText(Formato.texto(universidad.getNombre()));
            fila.dominio.setText(fila.itemView.getContext().getString(R.string.dominio_valor,
                    universidad.getDominio() == null ? "—" : universidad.getDominio()));
            fila.sitio.setText(universidad.getSitioWeb() == null ? "—" : universidad.getSitioWeb());
        }

        @Override
        public int getItemCount() {
            return universidades.size();
        }

        static class Fila extends RecyclerView.ViewHolder {
            final TextView nombre;
            final TextView dominio;
            final TextView sitio;

            Fila(View vista) {
                super(vista);
                nombre = vista.findViewById(R.id.nombreUniversidad);
                dominio = vista.findViewById(R.id.dominioUniversidad);
                sitio = vista.findViewById(R.id.sitioUniversidad);
            }
        }
    }
}
