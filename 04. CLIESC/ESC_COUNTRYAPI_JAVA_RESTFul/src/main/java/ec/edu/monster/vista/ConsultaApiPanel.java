package ec.edu.monster.vista;

import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.modelos.Pais;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * «Consultar país en la API» (ECUD3): busca por nombre en REST Countries (a través del servidor),
 * muestra los resultados y si cada país ya está guardado, y permite guardar el elegido.
 *
 * @author Escobar Marcos
 */
public final class ConsultaApiPanel extends PanelBase implements ConsultaApiVista {

    private final TablaModelo<Pais> modelo = new TablaModelo<>(
            TablaModelo.Columna.texto("Nombre", Pais::getNombre),
            TablaModelo.Columna.texto("Capital", Pais::getCapital),
            TablaModelo.Columna.texto("Región", Pais::getRegion),
            TablaModelo.Columna.texto("Subregión", Pais::getSubregion),
            TablaModelo.Columna.numero("Población", Pais::getPoblacion),
            TablaModelo.Columna.numero("Área (km²)", Pais::getArea),
            TablaModelo.Columna.texto("Moneda", Pais::getMoneda),
            TablaModelo.Columna.texto("¿Guardado?", p -> p.isGuardado() ? "Sí" : "No"));
    private final JTable tabla = Estilos.tabla(modelo);
    private final JTextField nombre = Estilos.campo(22);
    private final JButton consultar = Estilos.botonPrimario("Consultar");
    private final JButton guardar = Estilos.botonPrimario("Guardar");

    public ConsultaApiPanel(VentanaPrincipal ventana) {
        super(ventana, "Consultar país en la API",
                "Consulta REST Countries por nombre (por ejemplo «ecuador») y guarde el país elegido.");
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.setOpaque(false);
        izquierda.add(Estilos.etiqueta("Nombre del país", Estilos.FUENTE_NEGRITA, Estilos.TEXTO));
        izquierda.add(nombre);
        izquierda.add(consultar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(guardar);
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);

        contenido.add(barra, BorderLayout.NORTH);
        contenido.add(Estilos.desplazable(tabla), BorderLayout.CENTER);
    }

    public void setControlador(ApiControlador controlador) {
        consultar.addActionListener(e -> ejecutar(controlador::consultar));
        nombre.addActionListener(e -> ejecutar(controlador::consultar));
        guardar.addActionListener(e -> ejecutar(controlador::guardar));
    }

    @Override
    public String leerNombre() {
        return Estilos.enEdtYEsperar(nombre::getText);
    }

    @Override
    public void mostrarResultados(List<Pais> paises) {
        Estilos.enEdt(() -> {
            int fila = tabla.getSelectedRow();
            modelo.setFilas(paises);
            if (fila >= 0 && fila < tabla.getRowCount()) {
                tabla.setRowSelectionInterval(fila, fila);
            }
        });
    }

    @Override
    public Pais leerSeleccion() {
        return Estilos.enEdtYEsperar(() -> {
            int fila = tabla.getSelectedRow();
            return fila < 0 ? null : modelo.fila(tabla.convertRowIndexToModel(fila));
        });
    }
}
