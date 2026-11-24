package ec.edu.monster.vista;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import ec.edu.monster.R;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

/**
 * Punto verde o rojo con el texto «Servidor disponible» / «Servidor no disponible». Mientras
 * está visible consulta el servidor cada 10 segundos, en un hilo aparte.
 *
 * @author Escobar Marcos
 */
public class IndicadorServidor extends AppCompatTextView {

    private static final long INTERVALO_MS = 10_000;
    /** Un solo hilo para todos los indicadores: las consultas no se acumulan. */
    private static final ExecutorService HILO = Executors.newSingleThreadExecutor();

    private final Handler principal = new Handler(Looper.getMainLooper());
    private final boolean sobreAzul;
    private final Runnable comprobar = this::comprobarAhora;
    private BooleanSupplier comprobador;
    private boolean activo;

    public IndicadorServidor(@NonNull Context contexto) {
        this(contexto, null);
    }

    public IndicadorServidor(@NonNull Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);
        TypedArray valores = contexto.obtainStyledAttributes(atributos, R.styleable.IndicadorServidor);
        sobreAzul = valores.getBoolean(R.styleable.IndicadorServidor_sobreAzul, false);
        valores.recycle();
        setTextSize(13);
        setCompoundDrawablePadding(Math.round(8 * getResources().getDisplayMetrics().density));
        setTextColor(ContextCompat.getColor(contexto, sobreAzul ? R.color.blanco : R.color.texto_suave));
        setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        pintar(null);
    }

    /** Función que responde si el servidor está disponible (se llama fuera del hilo principal). */
    public void setComprobador(BooleanSupplier comprobador) {
        this.comprobador = comprobador;
        actualizarCiclo();
    }

    /** Consulta de inmediato (por ejemplo, después de cambiar la dirección del servidor). */
    public void comprobarAhora() {
        principal.removeCallbacks(comprobar);
        if (!activo || comprobador == null) {
            return;
        }
        BooleanSupplier consulta = comprobador;
        HILO.execute(() -> {
            boolean disponible = consulta.getAsBoolean();
            principal.post(() -> {
                pintar(disponible);
                if (activo) {
                    principal.postDelayed(comprobar, INTERVALO_MS);
                }
            });
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        actualizarCiclo();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        activo = false;
        principal.removeCallbacks(comprobar);
    }

    @Override
    public void onVisibilityAggregated(boolean visible) {
        super.onVisibilityAggregated(visible);
        actualizarCiclo();
    }

    /** Consulta solo mientras se ve en pantalla, para no gastar peticiones. */
    private void actualizarCiclo() {
        boolean debeEstarActivo = comprobador != null && isAttachedToWindow() && getVisibility() == View.VISIBLE
                && isShown();
        if (debeEstarActivo && !activo) {
            activo = true;
            pintar(null);
            comprobarAhora();
        } else if (!debeEstarActivo && activo) {
            activo = false;
            principal.removeCallbacks(comprobar);
        }
    }

    /** null: comprobando; true: disponible; false: no disponible. */
    private void pintar(Boolean disponible) {
        int color;
        if (disponible == null) {
            setText(R.string.comprobando_servidor);
            color = sobreAzul ? R.color.texto_sobre_azul : R.color.borde;
        } else if (disponible) {
            setText(R.string.servidor_disponible);
            color = R.color.verde;
        } else {
            setText(R.string.servidor_no_disponible);
            color = sobreAzul ? R.color.rojo_claro : R.color.rojo;
        }
        Drawable punto = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.punto).mutate());
        DrawableCompat.setTintList(punto, ColorStateList.valueOf(ContextCompat.getColor(getContext(), color)));
        setCompoundDrawablesRelativeWithIntrinsicBounds(punto, null, null, null);
    }
}
