package ec.edu.monster.vista;

import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.modelos.ResumenImportacion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * «Importar países» (ECUD2): pide confirmación, importa todos los países de REST Countries y
 * muestra cuántos se recibieron, registraron y actualizaron.
 *
 * @author Escobar Marcos
 */
public final class ImportacionPanel extends PanelBase implements ImportacionVista {

    private final JButton importar = Estilos.botonPrimario("Importar países");
    private final JLabel recibidos = cifra();
    private final JLabel registrados = cifra();
    private final JLabel actualizados = cifra();
    private final JPanel resumen = new JPanel(new GridLayout(1, 3, 16, 0));

    public ImportacionPanel(VentanaPrincipal ventana) {
        super(ventana, "Importar países",
                "Trae todos los países de la API REST Countries. Los nuevos se registran y los existentes se actualizan, sin duplicar.");
        JPanel accion = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        accion.setOpaque(false);
        accion.add(importar);

        resumen.setOpaque(false);
        resumen.add(caja("Recibidos de la API", recibidos));
        resumen.add(caja("Registrados (nuevos)", registrados));
        resumen.add(caja("Actualizados", actualizados));
        resumen.setVisible(false);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.add(accion, BorderLayout.NORTH);
        cuerpo.add(resumen, BorderLayout.CENTER);
        contenido.add(cuerpo, BorderLayout.NORTH);
    }

    public void setControlador(ApiControlador controlador) {
        importar.addActionListener(e -> ejecutar(controlador::importar));
    }

    @Override
    public boolean confirmarImportacion() {
        return Estilos.enEdtYEsperar(() -> JOptionPane.showOptionDialog(this,
                "¿Importar los países desde la API REST Countries? Los países existentes se actualizarán",
                "Importar países", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new Object[] {"Importar", "Cancelar"}, "Importar") == 0);
    }

    @Override
    public void mostrarResumen(ResumenImportacion r) {
        Estilos.enEdt(() -> {
            recibidos.setText(Estilos.formatearEntero(r.getRecibidos()));
            registrados.setText(Estilos.formatearEntero(r.getRegistrados()));
            actualizados.setText(Estilos.formatearEntero(r.getActualizados()));
            resumen.setVisible(true);
            mostrarMensaje("Importación finalizada: " + r.getRecibidos() + " países recibidos, "
                    + r.getRegistrados() + " registrados y " + r.getActualizados() + " actualizados");
        });
    }

    private static JLabel cifra() {
        return Estilos.etiqueta("0", Estilos.FUENTE_CIFRA, Estilos.AZUL);
    }

    private static JPanel caja(String titulo, JLabel valor) {
        JPanel caja = Estilos.tarjeta();
        caja.setLayout(new BoxLayout(caja, BoxLayout.Y_AXIS));
        caja.add(Estilos.textoSuave(titulo));
        caja.add(valor);
        caja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, Estilos.AZUL),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return caja;
    }
}
