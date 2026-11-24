package ec.edu.monster.vista;

import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.Pais;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Formulario modal con los siete datos del país (ECUD4: Nombre, Capital, Región como lista
 * desplegable, Subregión, Población, Área (km²) y Moneda). Devuelve lo escrito sin validar;
 * la validación la hace el controlador con ValidadorDatos.
 *
 * @author Escobar Marcos
 */
final class FormularioPaisDialogo extends JDialog {

    private static final String ELIJA_REGION = "Seleccione...";

    private final JTextField nombre = Estilos.campo(24);
    private final JTextField capital = Estilos.campo(24);
    private final JComboBox<String> region = new JComboBox<>();
    private final JTextField subregion = Estilos.campo(24);
    private final JTextField poblacion = Estilos.campo(24);
    private final JTextField area = Estilos.campo(24);
    private final JTextField moneda = Estilos.campo(24);
    private Pais resultado;

    private FormularioPaisDialogo(Window duenio, Pais inicial) {
        super(duenio, inicial == null || inicial.getId() == 0 && inicial.getNombre() == null
                ? "Nuevo país" : "Datos del país", ModalityType.APPLICATION_MODAL);
        region.setFont(Estilos.FUENTE);
        region.addItem(ELIJA_REGION);
        Pais.REGIONES.forEach(region::addItem);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(Estilos.BLANCO);
        campos.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));
        fila(campos, 0, "Nombre *", nombre);
        fila(campos, 1, "Capital", capital);
        fila(campos, 2, "Región *", region);
        fila(campos, 3, "Subregión", subregion);
        fila(campos, 4, "Población *", poblacion);
        fila(campos, 5, "Área (km²)", area);
        fila(campos, 6, "Moneda", moneda);

        JButton guardar = Estilos.botonPrimario("Guardar");
        JButton cancelar = Estilos.botonSecundario("Cancelar");
        guardar.addActionListener(e -> aceptar());
        cancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBackground(Estilos.BLANCO);
        botones.setBorder(BorderFactory.createEmptyBorder(8, 24, 20, 24));
        botones.add(cancelar);
        botones.add(guardar);

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(Estilos.BLANCO);
        cuerpo.add(campos, BorderLayout.CENTER);
        cuerpo.add(botones, BorderLayout.SOUTH);
        setContentPane(cuerpo);
        getRootPane().setDefaultButton(guardar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        llenar(inicial);
        pack();
        setResizable(false);
        setLocationRelativeTo(duenio);
    }

    /**
     * Abre el formulario y espera a que el usuario lo cierre.
     *
     * @return el país escrito, o null si canceló
     */
    static Pais pedir(Window duenio, Pais inicial) {
        FormularioPaisDialogo dialogo = new FormularioPaisDialogo(duenio, inicial);
        dialogo.setVisible(true);
        return dialogo.resultado;
    }

    private static void fila(JPanel panel, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(5, 0, 5, 12);
        c.anchor = GridBagConstraints.WEST;
        panel.add(Estilos.etiqueta(etiqueta, Estilos.FUENTE_NEGRITA, Estilos.TEXTO), c);
        c.gridx = 1;
        c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo, c);
    }

    private void llenar(Pais p) {
        if (p == null) {
            return;
        }
        nombre.setText(texto(p.getNombre()));
        capital.setText(texto(p.getCapital()));
        region.setSelectedItem(p.getRegion() != null && Pais.REGIONES.contains(p.getRegion()) ? p.getRegion() : ELIJA_REGION);
        subregion.setText(texto(p.getSubregion()));
        // Un valor inválido escrito antes (-1 / NaN) vuelve como campo vacío para corregirlo.
        poblacion.setText(p.getPoblacion() < 0 ? "" : Long.toString(p.getPoblacion()));
        area.setText(Double.isNaN(p.getArea()) || p.getArea() < 0 ? "" : Estilos.numeroEditable(p.getArea()));
        moneda.setText(texto(p.getMoneda()));
    }

    private void aceptar() {
        String regionElegida = (String) region.getSelectedItem();
        resultado = new Pais(
                nombre.getText().trim(),
                vacioANulo(capital.getText()),
                ELIJA_REGION.equals(regionElegida) ? null : regionElegida,
                vacioANulo(subregion.getText()),
                ValidadorDatos.leerPoblacion(poblacion.getText()),
                ValidadorDatos.leerArea(area.getText()),
                vacioANulo(moneda.getText()));
        dispose();
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
