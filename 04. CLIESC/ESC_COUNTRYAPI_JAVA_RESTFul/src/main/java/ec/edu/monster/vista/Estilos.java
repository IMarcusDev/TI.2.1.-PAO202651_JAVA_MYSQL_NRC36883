package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Paleta, fuentes y fábricas de componentes compartidas por las pantallas Swing. Los colores son
 * los del diseño de referencia (estilos.css del cliente web de EXAMEN).
 *
 * @author Escobar Marcos
 */
public final class Estilos {

    public static final String TITULO_APLICACION = "CountryAPI";

    public static final Color AZUL = new Color(0x1F4E79);
    public static final Color AZUL_OSCURO = new Color(0x183D60);
    public static final Color AZUL_CLARO = new Color(0xE8EFF7);
    public static final Color FONDO = new Color(0xF3F4F6);
    public static final Color TEXTO = new Color(0x1F2933);
    public static final Color TEXTO_SUAVE = new Color(0x4B5563);
    public static final Color BORDE = new Color(0xD1D5DB);
    public static final Color VERDE = new Color(0x22C55E);
    public static final Color VERDE_CLARO = new Color(0xE9F9EF);
    public static final Color VERDE_TEXTO = new Color(0x166534);
    public static final Color ROJO = new Color(0xB91C1C);
    public static final Color ROJO_CLARO = new Color(0xFDECEC);
    public static final Color BLANCO = Color.WHITE;

    public static final Font FUENTE = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font FUENTE_NEGRITA = FUENTE.deriveFont(Font.BOLD);
    public static final Font FUENTE_PEQUENA = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FUENTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static final Font FUENTE_SUBTITULO = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font FUENTE_CIFRA = new Font(Font.SANS_SERIF, Font.BOLD, 28);

    private static final Locale ECUADOR = Locale.forLanguageTag("es-EC");

    private Estilos() {
    }

    // ------------------------------------------------------------------ textos

    public static JLabel titulo(String texto) {
        return etiqueta(texto, FUENTE_TITULO, AZUL);
    }

    public static JLabel subtitulo(String texto) {
        return etiqueta(texto, FUENTE_SUBTITULO, AZUL);
    }

    public static JLabel texto(String texto) {
        return etiqueta(texto, FUENTE, TEXTO);
    }

    public static JLabel textoSuave(String texto) {
        return etiqueta(texto, FUENTE_PEQUENA, TEXTO_SUAVE);
    }

    public static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente);
        etiqueta.setForeground(color);
        return etiqueta;
    }

    // ------------------------------------------------------------------ controles

    public static JTextField campo(int columnas) {
        JTextField campo = new JTextField(columnas);
        estiloEntrada(campo);
        return campo;
    }

    public static <T extends JComponent> T estiloEntrada(T componente) {
        componente.setFont(FUENTE);
        componente.setForeground(TEXTO);
        componente.setBackground(BLANCO);
        componente.setBorder(bordeEntrada());
        return componente;
    }

    public static Border bordeEntrada() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    public static JButton botonPrimario(String texto) {
        return boton(texto, AZUL, BLANCO, AZUL);
    }

    public static JButton botonSecundario(String texto) {
        return boton(texto, BLANCO, AZUL, BORDE);
    }

    public static JButton botonPeligro(String texto) {
        return boton(texto, BLANCO, ROJO, ROJO);
    }

    /** Botón plano de la barra superior azul. */
    public static JButton botonBarra(String texto) {
        JButton boton = boton(texto, AZUL, BLANCO, AZUL);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return boton;
    }

    private static JButton boton(String texto, Color fondo, Color letra, Color borde) {
        JButton boton = new JButton(texto);
        // BasicButtonUI pinta el fondo plano con el color indicado en cualquier look and feel.
        boton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        boton.setFont(FUENTE_NEGRITA);
        boton.setBackground(fondo);
        boton.setForeground(letra);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(true);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /** Panel blanco con borde gris, como las tarjetas del diseño web. */
    public static JPanel tarjeta() {
        JPanel panel = new JPanel();
        panel.setBackground(BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    // ------------------------------------------------------------------ tablas

    /** Tabla con encabezado celeste, filas alternas y números alineados a la derecha. */
    public static JTable tabla(TablaModelo<?> modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(FUENTE);
        tabla.setForeground(TEXTO);
        tabla.setRowHeight(28);
        tabla.setGridColor(BORDE);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(AZUL_CLARO);
        tabla.setSelectionForeground(TEXTO);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco,
                    int fila, int columna) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(t, valor, sel, foco, fila, columna);
                celda.setBackground(AZUL_CLARO);
                celda.setForeground(AZUL);
                celda.setFont(FUENTE_NEGRITA);
                celda.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)));
                celda.setHorizontalAlignment(modelo.esNumerica(columna) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return celda;
            }
        });
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco,
                    int fila, int columna) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(t, valor, sel, foco, fila, columna);
                int columnaModelo = t.convertColumnIndexToModel(columna);
                boolean numerica = modelo.esNumerica(columnaModelo);
                celda.setHorizontalAlignment(numerica ? SwingConstants.RIGHT : SwingConstants.LEFT);
                celda.setText(numerica && valor instanceof Number n ? formatearNumero(n) : valor == null ? "" : valor.toString());
                celda.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    celda.setBackground(fila % 2 == 0 ? BLANCO : FONDO);
                }
                return celda;
            }
        });
        return tabla;
    }

    public static JScrollPane desplazable(JComponent contenido) {
        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(BLANCO);
        return scroll;
    }

    // ------------------------------------------------------------------ números

    /** Entero con separador de miles (RNF-009), por ejemplo 17.643.060. */
    public static String formatearEntero(long valor) {
        return NumberFormat.getIntegerInstance(ECUADOR).format(valor);
    }

    /** Número con separador de miles y hasta dos decimales. */
    public static String formatearDecimal(double valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(ECUADOR);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

    static String formatearNumero(Number numero) {
        if (numero instanceof Double || numero instanceof Float) {
            return formatearDecimal(numero.doubleValue());
        }
        return formatearEntero(numero.longValue());
    }

    /** Número sin separadores para llenar un campo editable (276841.5 → «276841,5»). */
    public static String numeroEditable(double valor) {
        if (valor == Math.rint(valor)) {
            return Long.toString((long) valor);
        }
        return Double.toString(valor).replace('.', ',');
    }

    // ------------------------------------------------------------------ hilos

    /** Ejecuta en el hilo de Swing (EDT): al momento si ya está en él. */
    public static void enEdt(Runnable accion) {
        if (SwingUtilities.isEventDispatchThread()) {
            accion.run();
        } else {
            SwingUtilities.invokeLater(accion);
        }
    }

    /**
     * Ejecuta en el EDT y espera el resultado. Lo usan las vistas cuando un controlador, que corre
     * en segundo plano, lee un campo o abre un diálogo.
     */
    public static <T> T enEdtYEsperar(Supplier<T> accion) {
        if (SwingUtilities.isEventDispatchThread()) {
            return accion.get();
        }
        AtomicReference<T> resultado = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> resultado.set(accion.get()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
        return resultado.get();
    }

    /** Punto de color para el indicador del servidor. */
    public static Icon punto(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y, 10, 10);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    /** Separación vertical u horizontal fija. */
    public static Component espacio(int ancho, int alto) {
        JPanel espacio = new JPanel();
        espacio.setOpaque(false);
        espacio.setPreferredSize(new Dimension(ancho, alto));
        espacio.setMaximumSize(new Dimension(ancho, alto));
        return espacio;
    }
}
