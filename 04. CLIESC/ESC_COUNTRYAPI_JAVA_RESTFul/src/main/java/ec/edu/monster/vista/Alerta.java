package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Aviso dentro de la pantalla: verde para éxito, rojo para error (como las alertas del diseño
 * web). Se oculta al cambiar de pantalla.
 *
 * @author Escobar Marcos
 */
final class Alerta extends JPanel {

    private final JLabel texto = new JLabel();

    Alerta() {
        super(new BorderLayout());
        texto.setFont(Estilos.FUENTE_NEGRITA);
        add(texto, BorderLayout.CENTER);
        setVisible(false);
    }

    void exito(String mensaje) {
        mostrar(mensaje, Estilos.VERDE_CLARO, Estilos.VERDE, Estilos.VERDE_TEXTO);
    }

    void error(String mensaje) {
        mostrar(mensaje, Estilos.ROJO_CLARO, Estilos.ROJO, Estilos.ROJO);
    }

    void ocultar() {
        setVisible(false);
    }

    private void mostrar(String mensaje, Color fondo, Color borde, Color letra) {
        // HTML para que los mensajes largos ocupen varias líneas.
        texto.setText("<html>" + escapar(mensaje) + "</html>");
        texto.setForeground(letra);
        setBackground(fondo);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, borde),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        setVisible(true);
        revalidate();
        repaint();
    }

    private static String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
