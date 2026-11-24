package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.modelos.Pais;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Países (ECUD4): JTable con los siete datos, campo «Buscar» y botones Nuevo, Editar y
 * Eliminar. Doble clic en una fila también abre «Editar».
 *
 * @author Escobar Marcos
 */
public final class PaisPanel extends PanelBase implements PaisVista {

    private final TablaModelo<Pais> modelo = new TablaModelo<>(
            TablaModelo.Columna.texto("Nombre", Pais::getNombre),
            TablaModelo.Columna.texto("Capital", Pais::getCapital),
            TablaModelo.Columna.texto("Región", Pais::getRegion),
            TablaModelo.Columna.texto("Subregión", Pais::getSubregion),
            TablaModelo.Columna.numero("Población", Pais::getPoblacion),
            TablaModelo.Columna.numero("Área (km²)", Pais::getArea),
            TablaModelo.Columna.texto("Moneda", Pais::getMoneda));
    private final JTable tabla = Estilos.tabla(modelo);
    private final JTextField filtro = Estilos.campo(24);
    private final JButton buscar = Estilos.botonPrimario("Buscar");
    private final JButton nuevo = Estilos.botonPrimario("Nuevo");
    private final JButton editar = Estilos.botonSecundario("Editar");
    private final JButton eliminar = Estilos.botonPeligro("Eliminar");
    private final JLabel total = Estilos.textoSuave(" ");

    public PaisPanel(VentanaPrincipal ventana) {
        super(ventana, "Países", "Países almacenados en la base de datos. Busque por nombre o mantenga el listado.");
        nuevo.setMnemonic(KeyEvent.VK_N);
        editar.setMnemonic(KeyEvent.VK_E);
        eliminar.setMnemonic(KeyEvent.VK_L);
        buscar.setMnemonic(KeyEvent.VK_B);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.setOpaque(false);
        izquierda.add(Estilos.etiqueta("Buscar", Estilos.FUENTE_NEGRITA, Estilos.TEXTO));
        izquierda.add(filtro);
        izquierda.add(buscar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(nuevo);
        derecha.add(editar);
        derecha.add(eliminar);
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);

        contenido.add(barra, BorderLayout.NORTH);
        contenido.add(Estilos.desplazable(tabla), BorderLayout.CENTER);
        contenido.add(total, BorderLayout.SOUTH);
    }

    /** Conecta los botones con el controlador (cada acción corre en segundo plano). */
    public void setControlador(PaisControlador controlador) {
        buscar.addActionListener(e -> ejecutar(controlador::buscar));
        filtro.addActionListener(e -> ejecutar(controlador::buscar));
        nuevo.addActionListener(e -> ejecutar(controlador::registrar));
        editar.addActionListener(e -> ejecutar(controlador::actualizar));
        eliminar.addActionListener(e -> ejecutar(controlador::eliminar));
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    ejecutar(controlador::actualizar);
                }
            }
        });
    }

    @Override
    public void mostrarTabla(List<Pais> paises) {
        Estilos.enEdt(() -> {
            modelo.setFilas(paises);
            total.setText(paises.size() == 1 ? "1 país" : paises.size() + " países");
        });
    }

    @Override
    public String leerFiltro() {
        return Estilos.enEdtYEsperar(filtro::getText);
    }

    @Override
    public Pais leerPais(Pais inicial) {
        return Estilos.enEdtYEsperar(() -> FormularioPaisDialogo.pedir(SwingUtilities.getWindowAncestor(this), inicial));
    }

    @Override
    public Pais paisSeleccionado() {
        return Estilos.enEdtYEsperar(() -> {
            int fila = tabla.getSelectedRow();
            return fila < 0 ? null : modelo.fila(tabla.convertRowIndexToModel(fila));
        });
    }

    @Override
    public boolean confirmarEliminacion(Pais pais) {
        return Estilos.enEdtYEsperar(() -> JOptionPane.showOptionDialog(this,
                "¿Eliminar el país " + pais.getNombre() + "?", "Eliminar país",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                new Object[] {"Eliminar", "Cancelar"}, "Cancelar") == 0);
    }
}
