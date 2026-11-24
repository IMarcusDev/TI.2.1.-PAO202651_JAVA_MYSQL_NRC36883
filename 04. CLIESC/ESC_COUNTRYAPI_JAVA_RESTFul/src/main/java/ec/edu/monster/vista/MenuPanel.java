package ec.edu.monster.vista;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * Menú principal con una tarjeta por opción (como el menú de tarjetas del diseño web). Cada
 * tarjeta es un botón, así que también se usa con el teclado (Tab y Enter).
 *
 * @author Escobar Marcos
 */
final class MenuPanel extends PanelBase {

    private static final Border BORDE_NORMAL = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilos.BORDE),
            BorderFactory.createEmptyBorder(18, 18, 18, 18));
    private static final Border BORDE_RESALTADO = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilos.AZUL, 2),
            BorderFactory.createEmptyBorder(17, 17, 17, 17));

    private final JPanel tarjetas = new JPanel(new GridLayout(0, 3, 16, 16));

    MenuPanel(VentanaPrincipal ventana) {
        super(ventana, "Menú principal", "Elija una opción.");
        tarjetas.setOpaque(false);
        JPanel arriba = new JPanel(new java.awt.BorderLayout());
        arriba.setOpaque(false);
        arriba.add(tarjetas, java.awt.BorderLayout.NORTH);
        contenido.add(arriba, java.awt.BorderLayout.CENTER);
    }

    void saludar(String nombre) {
        // El subtítulo del encabezado se reemplaza por el saludo.
        mostrarMensaje("Bienvenido, " + nombre + ".");
    }

    void agregarOpcion(String nombre, String descripcion, Runnable accion) {
        JButton tarjeta = new JButton("<html><div style='width:230px'><b style='font-size:14pt;color:#1F4E79'>"
                + nombre + "</b><br><br><span style='color:#4B5563'>" + descripcion + "</span></div></html>");
        tarjeta.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        tarjeta.setFont(Estilos.FUENTE);
        tarjeta.setHorizontalAlignment(SwingConstants.LEFT);
        tarjeta.setVerticalAlignment(SwingConstants.TOP);
        tarjeta.setBackground(Estilos.BLANCO);
        tarjeta.setOpaque(true);
        tarjeta.setFocusPainted(false);
        tarjeta.setBorder(BORDE_NORMAL);
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tarjeta.addActionListener(e -> accion.run());
        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBorder(BORDE_RESALTADO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBorder(tarjeta.hasFocus() ? BORDE_RESALTADO : BORDE_NORMAL);
            }
        });
        tarjeta.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tarjeta.setBorder(BORDE_RESALTADO);
            }

            @Override
            public void focusLost(FocusEvent e) {
                tarjeta.setBorder(BORDE_NORMAL);
            }
        });
        tarjetas.add(tarjeta);
    }
}
