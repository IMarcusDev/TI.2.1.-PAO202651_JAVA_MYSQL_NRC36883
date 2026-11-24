package ec.edu.monster.vista;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import ec.edu.monster.R;
import ec.edu.monster.modelos.Pais;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Lista de países en tarjetas (la tabla de los otros clientes). Se usa en Países, Buscar y
 * Consultar en la API; en este último cada tarjeta muestra «Guardado» y el botón Guardar.
 *
 * @author Escobar Marcos
 */
class PaisAdaptador extends RecyclerView.Adapter<PaisAdaptador.Fila> {

    private final boolean modoConsulta;
    private final List<Pais> paises = new ArrayList<>();
    private Pais seleccionado;
    private BiConsumer<Pais, View> alTocar;
    private Consumer<Pais> alGuardar;

    /** @param modoConsulta true en «Consultar país en la API» */
    PaisAdaptador(boolean modoConsulta) {
        this.modoConsulta = modoConsulta;
    }

    void setAlTocar(BiConsumer<Pais, View> alTocar) {
        this.alTocar = alTocar;
    }

    void setAlGuardar(Consumer<Pais> alGuardar) {
        this.alGuardar = alGuardar;
    }

    @SuppressLint("NotifyDataSetChanged")
    void setPaises(List<Pais> nuevos) {
        paises.clear();
        if (nuevos != null) {
            paises.addAll(nuevos);
        }
        if (seleccionado != null && !paises.contains(seleccionado)) {
            seleccionado = null;
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    void seleccionar(Pais pais) {
        seleccionado = pais;
        notifyDataSetChanged();
    }

    Pais getSeleccionado() {
        return seleccionado;
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View vista = LayoutInflater.from(padre.getContext()).inflate(R.layout.item_pais, padre, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Pais pais = paises.get(posicion);
        fila.nombre.setText(Formato.texto(pais.getNombre()));
        fila.ubicacion.setText(ubicacion(pais));
        fila.poblacion.setText(Formato.entero(pais.getPoblacion()));
        fila.area.setText(Formato.decimal(pais.getArea()));
        fila.moneda.setText(pais.getMoneda() == null ? "—" : pais.getMoneda());

        boolean elegido = pais == seleccionado;
        fila.tarjeta.setStrokeColor(ContextCompat.getColor(fila.tarjeta.getContext(), elegido ? R.color.azul : R.color.borde));
        fila.tarjeta.setStrokeWidth(Math.round(fila.tarjeta.getResources().getDisplayMetrics().density * (elegido ? 2 : 1)));
        if (alTocar != null) {
            fila.tarjeta.setOnClickListener(v -> alTocar.accept(pais, v));
        } else {
            // En «Buscar y filtrar» la lista solo se lee.
            fila.tarjeta.setOnClickListener(null);
            fila.tarjeta.setClickable(false);
        }

        if (modoConsulta) {
            fila.guardado.setVisibility(View.VISIBLE);
            fila.guardado.setText(pais.isGuardado() ? R.string.guardado_si : R.string.guardado_no);
            fila.guardado.setBackgroundResource(pais.isGuardado() ? R.drawable.pastilla_si : R.drawable.pastilla_no);
            fila.guardado.setTextColor(ContextCompat.getColor(fila.guardado.getContext(),
                    pais.isGuardado() ? R.color.verde_texto : R.color.texto_suave));
            fila.guardar.setVisibility(View.VISIBLE);
            fila.guardar.setOnClickListener(v -> {
                if (alGuardar != null) {
                    alGuardar.accept(pais);
                }
            });
        } else {
            fila.guardado.setVisibility(View.GONE);
            fila.guardar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return paises.size();
    }

    private static String ubicacion(Pais pais) {
        StringBuilder texto = new StringBuilder(Formato.texto(pais.getCapital()));
        for (String parte : new String[] {pais.getRegion(), pais.getSubregion()}) {
            if (parte != null && !parte.isEmpty()) {
                texto.append(texto.length() == 0 ? "" : " · ").append(parte);
            }
        }
        return texto.toString();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final MaterialCardView tarjeta;
        final TextView nombre;
        final TextView guardado;
        final TextView ubicacion;
        final TextView poblacion;
        final TextView area;
        final TextView moneda;
        final Button guardar;

        Fila(View vista) {
            super(vista);
            tarjeta = (MaterialCardView) vista;
            nombre = vista.findViewById(R.id.nombrePais);
            guardado = vista.findViewById(R.id.guardadoPais);
            ubicacion = vista.findViewById(R.id.ubicacionPais);
            poblacion = vista.findViewById(R.id.poblacionPais);
            area = vista.findViewById(R.id.areaPais);
            moneda = vista.findViewById(R.id.monedaPais);
            guardar = vista.findViewById(R.id.botonGuardarPais);
        }
    }
}
