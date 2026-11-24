package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.modelos.ConteoRegion;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.PromedioRegion;
import ec.edu.monster.modelos.Reporte;
import java.util.ArrayList;
import java.util.List;

/**
 * Reportes (ECUD6): país más poblado, cantidad de países por región y promedio poblacional por
 * región. Se generan al entrar y con «Actualizar».
 *
 * @author Escobar Marcos
 */
public class ReporteFragment extends FragmentoBase implements ReporteVista {

    private ReporteControlador controlador;
    private TextView masPoblado;
    private TextView poblacion;
    private LinearLayout tablaConteo;
    private LinearLayout tablaPromedio;
    private Reporte reporte;

    void setControlador(ReporteControlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        return inflador.inflate(R.layout.fragment_reportes, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        masPoblado = vista.findViewById(R.id.masPoblado);
        poblacion = vista.findViewById(R.id.poblacionMasPoblado);
        tablaConteo = vista.findViewById(R.id.tablaConteo);
        tablaPromedio = vista.findViewById(R.id.tablaPromedio);
        vista.findViewById(R.id.botonActualizar).setOnClickListener(v -> {
            if (controlador != null) {
                ejecutar(controlador::generar);
            }
        });
        pintar();
    }

    private void pintar() {
        if (masPoblado == null) {
            return;
        }
        Pais pais = reporte == null ? null : reporte.getPaisMasPoblado();
        masPoblado.setText(pais == null ? getString(R.string.sin_dato) : pais.getNombre());
        poblacion.setText(pais == null ? "" : getString(R.string.habitantes_region, Formato.entero(pais.getPoblacion()), pais.getRegion()));

        List<String[]> conteo = new ArrayList<>();
        if (reporte != null && reporte.getPaisesPorRegion() != null) {
            for (ConteoRegion fila : reporte.getPaisesPorRegion()) {
                conteo.add(new String[] {fila.getRegion(), Formato.entero(fila.getCantidad())});
            }
        }
        Tabla.llenar(tablaConteo, getString(R.string.region), getString(R.string.paises), conteo);

        List<String[]> promedio = new ArrayList<>();
        if (reporte != null && reporte.getPromedioPorRegion() != null) {
            for (PromedioRegion fila : reporte.getPromedioPorRegion()) {
                promedio.add(new String[] {fila.getRegion(), Formato.entero(fila.getPromedioPoblacion())});
            }
        }
        Tabla.llenar(tablaPromedio, getString(R.string.region), getString(R.string.promedio_poblacion), promedio);
    }

    // ------------------------------------------------------------------ ReporteVista

    @Override
    public void mostrarReporte(Reporte reporte) {
        Hilos.enPrincipal(() -> {
            this.reporte = reporte;
            pintar();
        });
    }
}
