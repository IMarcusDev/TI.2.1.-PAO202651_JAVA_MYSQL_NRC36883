package ec.edu.monster.vista;

import ec.edu.monster.controlador.UniversidadControlador;
import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.modelos.Universidad;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Universidades (ECUD10): país en inglés (sugerido «Ecuador»), botones Importar y Ver guardadas,
 * y la tabla con nombre, dominio y sitio web.
 *
 * @author Escobar Marcos
 */
public final class UniversidadPanel extends PanelBase implements UniversidadVista {

    private final TablaModelo<Universidad> modelo = new TablaModelo<>(
            TablaModelo.Columna.texto("Nombre", Universidad::getNombre),
            TablaModelo.Columna.texto("Dominio", Universidad::getDominio),
            TablaModelo.Columna.texto("Sitio web", Universidad::getSitioWeb));
    private final JTable tabla = Estilos.tabla(modelo);
    private final JTextField pais = Estilos.campo(18);
    private final JButton importar = Estilos.botonPrimario("Importar");
    private final JButton verGuardadas = Estilos.botonSecundario("Ver guardadas");
    private final JLabel total = Estilos.textoSuave(" ");

    public UniversidadPanel(VentanaPrincipal ventana) {
        super(ventana, "Universidades",
                "Importa desde la API de universidades las de un país (escrito en inglés) y consulta las guardadas.");
        pais.setText("Ecuador");
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);
        barra.add(Estilos.etiqueta("País (en inglés)", Estilos.FUENTE_NEGRITA, Estilos.TEXTO));
        barra.add(pais);
        barra.add(importar);
        barra.add(verGuardadas);

        contenido.add(barra, BorderLayout.NORTH);
        contenido.add(Estilos.desplazable(tabla), BorderLayout.CENTER);
        contenido.add(total, BorderLayout.SOUTH);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(420);
    }

    public void setControlador(UniversidadControlador controlador) {
        importar.addActionListener(e -> ejecutar(controlador::importar));
        verGuardadas.addActionListener(e -> ejecutar(controlador::listar));
        pais.addActionListener(e -> ejecutar(controlador::listar));
    }

    @Override
    public String leerPais() {
        return Estilos.enEdtYEsperar(pais::getText);
    }

    @Override
    public void mostrarResumen(ResumenImportacion r) {
        mostrarMensaje("Importación de universidades finalizada: " + r.getRecibidos() + " recibidas, "
                + r.getRegistrados() + " registradas y " + r.getActualizados() + " actualizadas");
    }

    @Override
    public void mostrarUniversidades(List<Universidad> universidades) {
        Estilos.enEdt(() -> {
            modelo.setFilas(universidades);
            total.setText(universidades.size() == 1 ? "1 universidad" : universidades.size() + " universidades");
        });
    }
}
