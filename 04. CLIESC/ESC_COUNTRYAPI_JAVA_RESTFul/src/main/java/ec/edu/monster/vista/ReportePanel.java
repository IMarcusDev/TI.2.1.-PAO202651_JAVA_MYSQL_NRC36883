package ec.edu.monster.vista;

import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.modelos.ConteoRegion;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.PromedioRegion;
import ec.edu.monster.modelos.Reporte;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Reportes estadísticos (ECUD6): países por región, país más poblado y promedio poblacional por
 * región. Se generan al entrar y con el botón «Actualizar».
 *
 * @author Escobar Marcos
 */
public final class ReportePanel extends PanelBase implements ReporteVista {

    private final TablaModelo<ConteoRegion> conteo = new TablaModelo<>(
            TablaModelo.Columna.texto("Región", ConteoRegion::getRegion),
            TablaModelo.Columna.numero("Países", ConteoRegion::getCantidad));
    private final TablaModelo<PromedioRegion> promedio = new TablaModelo<>(
            TablaModelo.Columna.texto("Región", PromedioRegion::getRegion),
            TablaModelo.Columna.numero("Promedio de población", PromedioRegion::getPromedioPoblacion));
    private final JLabel masPoblado = Estilos.etiqueta("—", Estilos.FUENTE_CIFRA, Estilos.AZUL);
    private final JLabel poblacion = Estilos.textoSuave(" ");
    private final JButton actualizar = Estilos.botonSecundario("Actualizar");

    public ReportePanel(VentanaPrincipal ventana) {
        super(ventana, "Reportes", "Estadísticas calculadas sobre los países almacenados.");
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barra.setOpaque(false);
        barra.add(actualizar);

        JPanel tarjetaMasPoblado = Estilos.tarjeta();
        tarjetaMasPoblado.setLayout(new BoxLayout(tarjetaMasPoblado, BoxLayout.Y_AXIS));
        tarjetaMasPoblado.add(Estilos.subtitulo("País más poblado"));
        tarjetaMasPoblado.add(masPoblado);
        tarjetaMasPoblado.add(poblacion);
        tarjetaMasPoblado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, Estilos.AZUL),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel tablas = new JPanel(new GridLayout(1, 2, 16, 0));
        tablas.setOpaque(false);
        tablas.add(seccion("Cantidad de países por región", Estilos.tabla(conteo)));
        tablas.add(seccion("Promedio poblacional por región", Estilos.tabla(promedio)));

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.add(tarjetaMasPoblado, BorderLayout.NORTH);
        cuerpo.add(tablas, BorderLayout.CENTER);

        contenido.add(barra, BorderLayout.NORTH);
        contenido.add(cuerpo, BorderLayout.CENTER);
    }

    public void setControlador(ReporteControlador controlador) {
        actualizar.addActionListener(e -> ejecutar(controlador::generar));
    }

    @Override
    public void mostrarReporte(Reporte reporte) {
        Estilos.enEdt(() -> {
            conteo.setFilas(reporte.getPaisesPorRegion());
            promedio.setFilas(reporte.getPromedioPorRegion());
            Pais pais = reporte.getPaisMasPoblado();
            masPoblado.setText(pais == null ? "—" : pais.getNombre());
            poblacion.setText(pais == null ? " " : Estilos.formatearEntero(pais.getPoblacion()) + " habitantes · " + pais.getRegion());
        });
    }

    private static JPanel seccion(String titulo, JTable tabla) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(Estilos.subtitulo(titulo), BorderLayout.NORTH);
        JScrollPane scroll = Estilos.desplazable(tabla);
        scroll.setPreferredSize(new Dimension(300, 230));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
}
