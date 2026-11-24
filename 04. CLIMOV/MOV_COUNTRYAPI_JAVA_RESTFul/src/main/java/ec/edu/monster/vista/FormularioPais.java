package ec.edu.monster.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.Pais;
import java.util.function.Consumer;

/**
 * Formulario de un país (registrar o editar), como diálogo. Entrega el país escrito o null si
 * el usuario cancela. No valida: eso lo hace el controlador con ValidadorDatos, y si hay error
 * vuelve a abrir el formulario con lo escrito y el mensaje arriba.
 *
 * @author Escobar Marcos
 */
final class FormularioPais {

    private FormularioPais() {
    }

    /**
     * @param inicial   datos para llenar los campos, o null (país nuevo)
     * @param nuevo     true para el título «Nuevo país»
     * @param error     mensaje de la validación anterior, o null
     * @param respuesta recibe el país escrito, o null si se cancela
     */
    static void mostrar(Context contexto, Pais inicial, boolean nuevo, String error, Consumer<Pais> respuesta) {
        View contenido = LayoutInflater.from(contexto).inflate(R.layout.dialogo_pais, null);
        EditText nombre = contenido.findViewById(R.id.campoNombre);
        EditText capital = contenido.findViewById(R.id.campoCapital);
        MaterialAutoCompleteTextView region = contenido.findViewById(R.id.campoRegion);
        EditText subregion = contenido.findViewById(R.id.campoSubregion);
        EditText poblacion = contenido.findViewById(R.id.campoPoblacion);
        EditText area = contenido.findViewById(R.id.campoArea);
        EditText moneda = contenido.findViewById(R.id.campoMoneda);
        TextView aviso = contenido.findViewById(R.id.aviso);

        region.setSimpleItems(Pais.REGIONES.toArray(new String[0]));
        if (inicial != null) {
            nombre.setText(Formato.texto(inicial.getNombre()));
            capital.setText(Formato.texto(inicial.getCapital()));
            if (inicial.getRegion() != null && Pais.REGIONES.contains(inicial.getRegion())) {
                region.setText(inicial.getRegion(), false);
            }
            subregion.setText(Formato.texto(inicial.getSubregion()));
            // Un valor inválido escrito antes (-1 / NaN) vuelve como campo vacío para corregirlo.
            poblacion.setText(inicial.getPoblacion() < 0 ? "" : Long.toString(inicial.getPoblacion()));
            area.setText(Double.isNaN(inicial.getArea()) || inicial.getArea() < 0 ? "" : Formato.editable(inicial.getArea()));
            moneda.setText(Formato.texto(inicial.getMoneda()));
        }
        if (error != null) {
            aviso.setText(error);
            aviso.setBackgroundResource(R.drawable.aviso_error);
            aviso.setTextColor(ContextCompat.getColor(contexto, R.color.rojo));
            aviso.setVisibility(View.VISIBLE);
        }

        new MaterialAlertDialogBuilder(contexto)
                .setTitle(nuevo ? R.string.nuevo_pais : R.string.datos_pais)
                .setView(contenido)
                .setPositiveButton(R.string.guardar, (dialogo, boton) -> respuesta.accept(new Pais(
                        nombre.getText().toString().trim(),
                        Formato.vacioANulo(capital.getText()),
                        Formato.vacioANulo(region.getText()),
                        Formato.vacioANulo(subregion.getText()),
                        ValidadorDatos.leerPoblacion(poblacion.getText().toString()),
                        ValidadorDatos.leerArea(area.getText().toString()),
                        Formato.vacioANulo(moneda.getText()))))
                .setNegativeButton(R.string.cancelar, (dialogo, boton) -> respuesta.accept(null))
                // Cerrar con «atrás» o tocando fuera también cancela.
                .setOnDismissListener(dialogo -> respuesta.accept(null))
                .show();
    }
}
