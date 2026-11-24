package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * «Buscar y filtrar» (ECUD5): nombre y capital por coincidencia parcial, y filtros de región
 * (Todas, América, Europa, ...) que vuelven a consultar en cuanto se elige uno.
 *
 * @author Escobar Marcos
 */
public final class BusquedaPanel extends PanelBase implements BusquedaVista {

    private static final String TODAS = "Todas";

    private final TablaModelo<Pais> modelo = new TablaModelo<>(
            TablaModelo.Columna.texto("Nombre", Pais::getNombre),
            TablaModelo.Columna.texto("Capital", Pais::getCapital),
            TablaModelo.Columna.texto("Región", Pais::getRegion),
            TablaModelo.Columna.texto("Subregión", Pais::getSubregion),
            TablaModelo.Columna.numero("Población", Pais::getPoblacion),
            TablaModelo.Columna.numero("Área (km²)", Pais::getArea),
            TablaModelo.Columna.texto("Moneda", Pais::getMoneda));
    private final JTable tabla = Estilos.tabla(modelo);
    private final JTextField nombre = Estilos.campo(16);
    private final JTextField capital = Estilos.campo(16);
    private final JButton buscar = Estilos.botonPrimario("Buscar");
    private final JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final ButtonGroup grupoFiltros = new ButtonGroup();
    private final List<JToggleButton> botonesRegion = new ArrayList<>();
    private final JLabel total = Estilos.etiqueta(" ", Estilos.FUENTE_NEGRITA, Estilos.AZUL);
    private Runnable alCambiarRegion = () -> { };

    public BusquedaPanel(VentanaPrincipal ventana) {
        super(ventana, "Buscar y filtrar", "Combine nombre, capital y región. Al elegir una región la tabla se actualiza al momento.");
        JPanel criterios = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        criterios.setOpaque(false);
        criterios.add(Estilos.etiqueta("Nombre", Estilos.FUENTE_NEGRITA, Estilos.TEXTO));
        criterios.add(nombre);
        criterios.add(Estilos.etiqueta("Capital", Estilos.FUENTE_NEGRITA, Estilos.TEXTO));
        criterios.add(capital);
        criterios.add(buscar);

        filtros.setOpaque(false);
        filtros.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(criterios, BorderLayout.NORTH);
        arriba.add(filtros, BorderLayout.SOUTH);

        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(Estilos.desplazable(tabla), BorderLayout.CENTER);
        contenido.add(total, BorderLayout.SOUTH);
    }

    public void setControlador(PaisControlador controlador) {
        buscar.addActionListener(e -> ejecutar(controlador::buscarYFiltrar));
        nombre.addActionListener(e -> ejecutar(controlador::buscarYFiltrar));
        capital.addActionListener(e -> ejecutar(controlador::buscarYFiltrar));
        alCambiarRegion = () -> ejecutar(controlador::buscarYFiltrar);
    }

    @Override
    public CriteriosBusqueda leerCriterios() {
        return Estilos.enEdtYEsperar(() -> {
            String region = null;
            for (JToggleButton boton : botonesRegion) {
                if (boton.isSelected() && !TODAS.equals(boton.getText())) {
                    region = boton.getText();
                }
            }
            return new CriteriosBusqueda(nombre.getText(), capital.getText(), region);
        });
    }

    @Override
    public void mostrarFiltrosRegion(List<String> regiones) {
        Estilos.enEdt(() -> {
            filtros.removeAll();
            botonesRegion.forEach(grupoFiltros::remove);
            botonesRegion.clear();
            filtros.add(Estilos.etiqueta("Región:", Estilos.FUENTE_NEGRITA, Estilos.TEXTO));
            List<String> opciones = new ArrayList<>();
            opciones.add(TODAS);
            opciones.addAll(regiones);
            for (String opcion : opciones) {
                JToggleButton boton = chip(opcion);
                grupoFiltros.add(boton);
                botonesRegion.add(boton);
                filtros.add(boton);
            }
            botonesRegion.get(0).setSelected(true);
            pintarChips();
            filtros.revalidate();
        });
    }

    @Override
    public void mostrarResultados(List<Pais> paises, int total) {
        Estilos.enEdt(() -> {
            modelo.setFilas(paises);
            this.total.setText(total == 1 ? "1 país encontrado" : total + " países encontrados");
        });
    }

    /** Botón tipo «chip»: azul cuando está elegido. Al elegirlo se vuelve a buscar. */
    private JToggleButton chip(String texto) {
        JToggleButton boton = new JToggleButton(texto);
        boton.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
        boton.setFont(Estilos.FUENTE_NEGRITA);
        boton.setFocusPainted(true);
        boton.setContentAreaFilled(false);
        boton.setOpaque(true);
        boton.addActionListener(e -> {
            pintarChips();
            alCambiarRegion.run();
        });
        return boton;
    }

    private void pintarChips() {
        for (JToggleButton boton : botonesRegion) {
            boolean elegido = boton.isSelected();
            boton.setBackground(elegido ? Estilos.AZUL : Estilos.BLANCO);
            boton.setForeground(elegido ? Estilos.BLANCO : Estilos.AZUL);
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(elegido ? Estilos.AZUL : Estilos.BORDE),
                    BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        }
    }
}
