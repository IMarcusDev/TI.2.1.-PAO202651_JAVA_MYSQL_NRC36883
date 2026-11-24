package ec.edu.monster.vista;

import java.util.function.BooleanSupplier;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Indicador de la barra inferior: verde si el servidor responde, rojo si no. Se revisa al abrir
 * y cada 10 segundos, fuera del hilo de Swing.
 *
 * @author Escobar Marcos
 */
final class IndicadorServidor extends JLabel {

    private static final int INTERVALO_MS = 10_000;

    private final BooleanSupplier servidorDisponible;
    private boolean revisando;

    IndicadorServidor(BooleanSupplier servidorDisponible) {
        this.servidorDisponible = servidorDisponible;
        setFont(Estilos.FUENTE_PEQUENA);
        setForeground(Estilos.TEXTO_SUAVE);
        setIconTextGap(6);
        mostrar(null);
    }

    void iniciar() {
        revisar();
        Timer temporizador = new Timer(INTERVALO_MS, e -> revisar());
        temporizador.start();
    }

    private void revisar() {
        if (revisando) {
            return;
        }
        revisando = true;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return servidorDisponible.getAsBoolean();
            }

            @Override
            protected void done() {
                revisando = false;
                try {
                    mostrar(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (java.util.concurrent.ExecutionException e) {
                    mostrar(false);
                }
            }
        }.execute();
    }

    private void mostrar(Boolean disponible) {
        if (disponible == null) {
            setIcon(Estilos.punto(Estilos.BORDE));
            setText("Comprobando el servidor...");
        } else if (disponible) {
            setIcon(Estilos.punto(Estilos.VERDE));
            setText("Servidor disponible");
        } else {
            setIcon(Estilos.punto(Estilos.ROJO));
            setText("Servidor no disponible");
        }
    }
}
