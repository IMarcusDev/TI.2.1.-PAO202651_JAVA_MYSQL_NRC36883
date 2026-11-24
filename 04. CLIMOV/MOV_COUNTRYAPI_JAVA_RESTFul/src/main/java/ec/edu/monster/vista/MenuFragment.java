package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import ec.edu.monster.R;

/**
 * Menú principal: una tarjeta por opción, en el orden del ECUD1, y «Cerrar sesión». La interfaz
 * MenuVista la implementa {@link PrincipalActivity}, que es quien cambia de pantalla.
 *
 * @author Escobar Marcos
 */
public class MenuFragment extends FragmentoBase {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        return inflador.inflate(R.layout.fragment_menu, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        LinearLayout opciones = vista.findViewById(R.id.opciones);
        opcion(opciones, R.drawable.ic_importar, R.string.op_importar, R.string.op_importar_desc, PrincipalActivity.IMPORTAR);
        opcion(opciones, R.drawable.ic_consultar, R.string.op_consultar, R.string.op_consultar_desc, PrincipalActivity.CONSULTAR);
        opcion(opciones, R.drawable.ic_paises, R.string.op_paises, R.string.op_paises_desc, PrincipalActivity.PAISES);
        opcion(opciones, R.drawable.ic_filtro, R.string.op_buscar, R.string.op_buscar_desc, PrincipalActivity.BUSCAR);
        opcion(opciones, R.drawable.ic_reporte, R.string.op_reportes, R.string.op_reportes_desc, PrincipalActivity.REPORTES);
        opcion(opciones, R.drawable.ic_universidad, R.string.op_universidades, R.string.op_universidades_desc,
                PrincipalActivity.UNIVERSIDADES);
        opcion(opciones, R.drawable.ic_salir, R.string.cerrar_sesion, R.string.cerrar_sesion_descripcion, null);
    }

    /** Saludo al entrar («Bienvenido, nombre.»). */
    void saludar(String nombre) {
        mostrarMensaje(getString(R.string.bienvenido, nombre));
    }

    private void opcion(LinearLayout opciones, @DrawableRes int icono, @StringRes int titulo,
            @StringRes int descripcion, String pantalla) {
        View tarjeta = getLayoutInflater().inflate(R.layout.item_opcion, opciones, false);
        ((ImageView) tarjeta.findViewById(R.id.iconoOpcion)).setImageResource(icono);
        ((TextView) tarjeta.findViewById(R.id.tituloOpcion)).setText(titulo);
        ((TextView) tarjeta.findViewById(R.id.descripcionOpcion)).setText(descripcion);
        tarjeta.setOnClickListener(v -> {
            if (pantalla == null) {
                principal().cerrarSesion();
            } else {
                principal().mostrar(pantalla);
            }
        });
        opciones.addView(tarjeta);
    }
}
