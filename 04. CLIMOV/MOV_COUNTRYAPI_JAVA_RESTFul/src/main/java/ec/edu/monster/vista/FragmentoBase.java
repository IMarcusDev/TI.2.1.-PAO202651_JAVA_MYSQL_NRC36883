package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import ec.edu.monster.R;
import java.util.function.Consumer;

/**
 * Pantalla de la app (equivale al PanelBase de escritorio). Implementa la parte común de
 * {@link VistaBase}: el aviso verde o rojo de arriba y el regreso al inicio de sesión.
 *
 * <p>Los controladores llaman a estos métodos desde un hilo secundario; todo cambio de widgets
 * pasa por {@link Hilos}. El estado del aviso se guarda en campos para pintarlo aunque la vista
 * todavía no exista.</p>
 *
 * @author Escobar Marcos
 */
public abstract class FragmentoBase extends Fragment implements VistaBase {

    private String avisoTexto;
    private boolean avisoEsError;
    private volatile String ultimoError;

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        pintarAviso();
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        Hilos.enPrincipal(() -> {
            avisoTexto = mensaje;
            avisoEsError = false;
            pintarAviso();
        });
    }

    @Override
    public void mostrarError(String mensaje) {
        ultimoError = mensaje;
        Hilos.enPrincipal(() -> {
            avisoTexto = mensaje;
            avisoEsError = true;
            pintarAviso();
        });
    }

    @Override
    public void irAInicioSesion() {
        String mensaje = ultimoError;
        Hilos.enPrincipal(() -> {
            if (getActivity() instanceof PrincipalActivity) {
                ((PrincipalActivity) getActivity()).irAInicioSesion(mensaje);
            }
        });
    }

    /** Oculta el aviso (al entrar a la pantalla). Solo en el hilo principal. */
    void limpiarAviso() {
        ultimoError = null;
        avisoTexto = null;
        pintarAviso();
    }

    /** Ejecuta una acción del controlador en segundo plano con el indicador de carga. */
    protected void ejecutar(Runnable accion) {
        if (getActivity() instanceof PrincipalActivity) {
            ((PrincipalActivity) getActivity()).ejecutar(accion, this);
        }
    }

    /**
     * Muestra un diálogo y espera la respuesta del usuario (se llama desde el hilo del
     * controlador). Mientras el diálogo está abierto se oculta la barra de progreso.
     */
    protected <T> T esperarUsuario(Consumer<Consumer<T>> mostrarDialogo) {
        Hilos.enPrincipal(() -> esperandoUsuario(true));
        try {
            return Hilos.esperarRespuesta(mostrarDialogo);
        } finally {
            Hilos.enPrincipal(() -> esperandoUsuario(false));
        }
    }

    private void esperandoUsuario(boolean valor) {
        if (getActivity() instanceof PrincipalActivity) {
            ((PrincipalActivity) getActivity()).setEsperandoUsuario(valor);
        }
    }

    protected PrincipalActivity principal() {
        return (PrincipalActivity) requireActivity();
    }

    private void pintarAviso() {
        View raiz = getView();
        TextView aviso = raiz == null ? null : raiz.findViewById(R.id.aviso);
        if (aviso == null) {
            return;
        }
        if (avisoTexto == null || avisoTexto.isEmpty()) {
            aviso.setVisibility(View.GONE);
            return;
        }
        aviso.setText(avisoTexto);
        aviso.setBackgroundResource(avisoEsError ? R.drawable.aviso_error : R.drawable.aviso_exito);
        aviso.setTextColor(ContextCompat.getColor(aviso.getContext(), avisoEsError ? R.color.rojo : R.color.verde_texto));
        aviso.setVisibility(View.VISIBLE);
    }
}
