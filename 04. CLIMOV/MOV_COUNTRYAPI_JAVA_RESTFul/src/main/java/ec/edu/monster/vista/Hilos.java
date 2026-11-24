package ec.edu.monster.vista;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Paso de datos entre el hilo de la interfaz y el hilo donde corren los controladores.
 *
 * <p>Los controladores son los mismos del cliente de escritorio y llaman a la vista de forma
 * síncrona (por ejemplo {@code vista.leerPais(...)} espera a que el usuario cierre el
 * formulario). Por eso se ejecutan fuera del hilo principal y la vista usa estos métodos para
 * tocar los widgets en el hilo principal.</p>
 *
 * @author Escobar Marcos
 */
final class Hilos {

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private Hilos() {
    }

    static boolean enHiloPrincipal() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /** Ejecuta en el hilo principal: al momento si ya está en él. */
    static void enPrincipal(Runnable accion) {
        if (enHiloPrincipal()) {
            accion.run();
        } else {
            PRINCIPAL.post(accion);
        }
    }

    /** Ejecuta en el hilo principal y devuelve el resultado (por ejemplo, el texto de un campo). */
    static <T> T enPrincipalYEsperar(Callable<T> lectura) {
        try {
            if (enHiloPrincipal()) {
                return lectura.call();
            }
            FutureTask<T> tarea = new FutureTask<>(lectura);
            PRINCIPAL.post(tarea);
            return tarea.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lectura de la vista interrumpida", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("No se pudo leer la vista", e.getCause());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer la vista", e);
        }
    }

    /**
     * Muestra algo que responde más tarde (un diálogo) y bloquea el hilo actual hasta la
     * respuesta. Solo se cuenta la primera respuesta (botón o cierre del diálogo).
     *
     * @param mostrar recibe la función con la que el diálogo entrega su respuesta
     */
    static <T> T esperarRespuesta(Consumer<Consumer<T>> mostrar) {
        if (enHiloPrincipal()) {
            throw new IllegalStateException("No se puede esperar al usuario en el hilo principal");
        }
        CountDownLatch listo = new CountDownLatch(1);
        AtomicReference<T> respuesta = new AtomicReference<>();
        AtomicBoolean respondido = new AtomicBoolean(false);
        PRINCIPAL.post(() -> mostrar.accept(valor -> {
            if (respondido.compareAndSet(false, true)) {
                respuesta.set(valor);
                listo.countDown();
            }
        }));
        try {
            listo.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return respuesta.get();
    }
}
