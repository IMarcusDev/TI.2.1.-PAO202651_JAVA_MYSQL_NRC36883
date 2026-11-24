package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import ec.edu.monster.AplicacionCountryApi;
import ec.edu.monster.R;
import ec.edu.monster.config.ConfigServidor;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelos.LoginRequest;

/**
 * Inicio de sesión (ECUD1). Además muestra a qué servidor se conecta la app y permite
 * cambiar la dirección sin recompilar.
 *
 * @author Escobar Marcos
 */
public class LoginFragment extends FragmentoBase implements LoginVista {

    private LoginControlador controlador;
    private AplicacionCountryApi app;
    private EditText usuario;
    private EditText clave;
    private TextView textoServidor;
    private IndicadorServidor indicador;

    void setControlador(LoginControlador controlador, AplicacionCountryApi app) {
        this.controlador = controlador;
        this.app = app;
        conectarIndicador();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        return inflador.inflate(R.layout.fragment_login, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        usuario = vista.findViewById(R.id.campoUsuario);
        clave = vista.findViewById(R.id.campoClave);
        textoServidor = vista.findViewById(R.id.textoServidor);
        indicador = vista.findViewById(R.id.indicadorLogin);
        Button ingresar = vista.findViewById(R.id.botonIngresar);

        ingresar.setOnClickListener(v -> ingresar());
        clave.setOnEditorActionListener((campo, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                ingresar();
                return true;
            }
            return false;
        });
        vista.findViewById(R.id.botonServidor).setOnClickListener(v -> pedirServidor());
        conectarIndicador();
    }

    private void ingresar() {
        if (controlador != null) {
            ejecutar(controlador::iniciarSesion);
        }
    }

    private void conectarIndicador() {
        if (indicador != null && app != null) {
            textoServidor.setText(app.getCliente().getUrlBase());
            indicador.setComprobador(app.getCliente()::servidorDisponible);
        }
    }

    /** Diálogo para escribir la dirección del servidor (por ejemplo, la IP del equipo en la red). */
    private void pedirServidor() {
        View contenido = getLayoutInflater().inflate(R.layout.dialogo_servidor, null);
        TextInputLayout capa = contenido.findViewById(R.id.capaServidor);
        EditText campo = contenido.findViewById(R.id.campoServidor);
        campo.setText(app.getCliente().getUrlBase());
        AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cambiar_servidor)
                .setView(contenido)
                .setPositiveButton(R.string.guardar, null)
                .setNegativeButton(R.string.cancelar, null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = campo.getText().toString();
            if (!ConfigServidor.esUrlValida(url)) {
                capa.setError(getString(R.string.servidor_invalido));
                return;
            }
            String guardada = app.cambiarServidor(url);
            textoServidor.setText(guardada);
            indicador.comprobarAhora();
            mostrarMensaje(getString(R.string.servidor_guardado, guardada));
            dialogo.dismiss();
        }));
        dialogo.show();
    }

    // ------------------------------------------------------------------ LoginVista

    @Override
    public void mostrarFormulario() {
        Hilos.enPrincipal(() -> {
            limpiarAviso();
            if (usuario != null) {
                usuario.setText("");
                clave.setText("");
                usuario.requestFocus();
            }
        });
    }

    @Override
    public LoginRequest leerCredenciales() {
        return Hilos.enPrincipalYEsperar(() -> new LoginRequest(usuario.getText().toString(), clave.getText().toString()));
    }

    @Override
    public void limpiarClave() {
        Hilos.enPrincipal(() -> {
            if (clave != null) {
                clave.setText("");
            }
        });
    }

    @Override
    public void irAInicioSesion() {
        // Ya está en el inicio de sesión.
    }
}
