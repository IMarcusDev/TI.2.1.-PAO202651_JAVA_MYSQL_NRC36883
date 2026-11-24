package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.modelos.ResumenImportacion;

/**
 * «Importar países» (ECUD2): pide confirmación, importa todos los países de REST Countries a
 * través del servidor y muestra cuántos se recibieron, registraron y actualizaron.
 *
 * @author Escobar Marcos
 */
public class ImportacionFragment extends FragmentoBase implements ImportacionVista {

    private ApiControlador controlador;
    private TextView recibidos;
    private TextView registrados;
    private TextView actualizados;
    private ResumenImportacion resumen;

    void setControlador(ApiControlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        return inflador.inflate(R.layout.fragment_importacion, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        recibidos = caja(vista, R.id.cajaRecibidos, R.string.recibidos_api);
        registrados = caja(vista, R.id.cajaRegistrados, R.string.registrados_nuevos);
        actualizados = caja(vista, R.id.cajaActualizados, R.string.actualizados);
        vista.findViewById(R.id.botonImportar).setOnClickListener(v -> {
            if (controlador != null) {
                ejecutar(controlador::importar);
            }
        });
        pintarResumen();
    }

    private static TextView caja(View vista, @IdRes int id, @StringRes int etiqueta) {
        View caja = vista.findViewById(id);
        ((TextView) caja.findViewById(R.id.etiquetaCifra)).setText(etiqueta);
        return caja.findViewById(R.id.cifra);
    }

    private void pintarResumen() {
        if (recibidos == null || resumen == null) {
            return;
        }
        recibidos.setText(Formato.entero(resumen.getRecibidos()));
        registrados.setText(Formato.entero(resumen.getRegistrados()));
        actualizados.setText(Formato.entero(resumen.getActualizados()));
    }

    // ------------------------------------------------------------------ ImportacionVista

    @Override
    public boolean confirmarImportacion() {
        Boolean confirmado = esperarUsuario(respuesta -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.op_importar)
                .setMessage(R.string.importar_pregunta)
                .setPositiveButton(R.string.importar, (d, b) -> respuesta.accept(Boolean.TRUE))
                .setNegativeButton(R.string.cancelar, (d, b) -> respuesta.accept(Boolean.FALSE))
                .setOnDismissListener(d -> respuesta.accept(Boolean.FALSE))
                .show());
        return Boolean.TRUE.equals(confirmado);
    }

    @Override
    public void mostrarResumen(ResumenImportacion resumen) {
        Hilos.enPrincipal(() -> {
            this.resumen = resumen;
            pintarResumen();
            mostrarMensaje(getString(R.string.importacion_finalizada,
                    resumen.getRecibidos(), resumen.getRegistrados(), resumen.getActualizados()));
        });
    }
}
