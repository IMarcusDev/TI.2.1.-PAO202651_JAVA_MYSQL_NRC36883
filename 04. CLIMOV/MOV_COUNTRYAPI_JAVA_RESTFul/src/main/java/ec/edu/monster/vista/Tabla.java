package ec.edu.monster.vista;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import ec.edu.monster.R;
import java.util.List;

/**
 * Tabla pequeña de dos columnas con encabezado celeste (reportes). La segunda columna es
 * numérica y va alineada a la derecha.
 *
 * @author Escobar Marcos
 */
final class Tabla {

    private Tabla() {
    }

    static void llenar(LinearLayout tabla, String columna1, String columna2, List<String[]> filas) {
        tabla.removeAllViews();
        Context contexto = tabla.getContext();
        LinearLayout encabezado = fila(contexto, columna1, columna2, R.style.EncabezadoTabla);
        encabezado.setBackgroundColor(ContextCompat.getColor(contexto, R.color.azul_claro));
        tabla.addView(encabezado);
        for (int i = 0; i < filas.size(); i++) {
            if (i > 0) {
                View divisor = new View(contexto);
                divisor.setBackgroundResource(R.drawable.divisor);
                tabla.addView(divisor, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        Math.max(1, Math.round(contexto.getResources().getDisplayMetrics().density))));
            }
            tabla.addView(fila(contexto, filas.get(i)[0], filas.get(i)[1], R.style.CeldaTabla));
        }
    }

    private static LinearLayout fila(Context contexto, String texto1, String texto2, int estilo) {
        LinearLayout fila = new LinearLayout(contexto);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.addView(celda(contexto, texto1, estilo, Gravity.START), new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        fila.addView(celda(contexto, texto2, estilo, Gravity.END), new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return fila;
    }

    private static TextView celda(Context contexto, String texto, int estilo, int alineacion) {
        TextView celda = new TextView(contexto);
        TextViewCompat.setTextAppearance(celda, estilo);
        int relleno = Math.round(10 * contexto.getResources().getDisplayMetrics().density);
        celda.setPadding(relleno, relleno, relleno, relleno);
        celda.setGravity(alineacion);
        celda.setText(texto);
        return celda;
    }
}
