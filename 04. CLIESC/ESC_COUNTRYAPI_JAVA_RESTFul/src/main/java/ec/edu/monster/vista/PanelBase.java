package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Pantalla Swing de la aplicación: título, descripción, alerta y contenido. Implementa
 * {@link VistaBase}; las subclases implementan además la interfaz de su módulo.
 *
 * <p>Los controladores corren fuera del hilo de Swing ({@link #ejecutar(Runnable)}), así que
 * los métodos de la vista pasan al EDT antes de tocar los componentes.</p>
 *
 * @author Escobar Marcos
 */
public abstract class PanelBase extends JPanel implements VistaBase {

    protected final VentanaPrincipal ventana;
    protected final JPanel contenido = new JPanel(new BorderLayout(0, 12));
    private final Alerta alerta = new Alerta();
    private volatile String ultimoError;

    PanelBase(VentanaPrincipal ventana, String titulo, String descripcion) {
        super(new BorderLayout(0, 12));
        this.ventana = ventana;
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.add(alinear(Estilos.titulo(titulo)));
        if (descripcion != null) {
            encabezado.add(Box.createVerticalStrut(4));
            encabezado.add(alinear(Estilos.textoSuave(descripcion)));
        }
        encabezado.add(Box.createVerticalStrut(12));
        encabezado.add(alinear(alerta));

        contenido.setOpaque(false);
        add(encabezado, BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
    }

    private static Component alinear(javax.swing.JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        Estilos.enEdt(() -> alerta.exito(mensaje));
    }

    @Override
    public void mostrarError(String mensaje) {
        ultimoError = mensaje;
        Estilos.enEdt(() -> alerta.error(mensaje));
    }

    @Override
    public void irAInicioSesion() {
        String mensaje = ultimoError;
        Estilos.enEdt(() -> ventana.irAInicioSesion(mensaje));
    }

    /** Oculta la alerta (al entrar a la pantalla). */
    void limpiarAviso() {
        ultimoError = null;
        alerta.ocultar();
    }

    /** Ejecuta una acción del controlador en segundo plano con el indicador de carga. */
    protected void ejecutar(Runnable accion) {
        ventana.ejecutar(accion, this);
    }
}
