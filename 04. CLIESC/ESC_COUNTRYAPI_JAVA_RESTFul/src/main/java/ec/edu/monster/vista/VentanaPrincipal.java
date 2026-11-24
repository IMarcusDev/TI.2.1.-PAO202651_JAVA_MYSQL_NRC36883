package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

/**
 * Única ventana de la aplicación. Tiene dos tarjetas: el inicio de sesión y la aplicación
 * (barra superior azul con la navegación y el usuario, la pantalla activa y la barra inferior con
 * el indicador del servidor). Implementa {@link MenuVista}.
 *
 * @author Escobar Marcos
 */
public final class VentanaPrincipal extends JFrame implements MenuVista {

    static final String ERROR_INESPERADO = "Ocurrió un error inesperado, intente nuevamente";

    private static final Logger LOG = Logger.getLogger(VentanaPrincipal.class.getName());
    private static final String TARJETA_LOGIN = "login";
    private static final String TARJETA_APP = "app";
    public static final String MENU = "menu";

    private final CardLayout tarjetasRaiz = new CardLayout();
    private final JPanel raiz = new JPanel(tarjetasRaiz);
    private final CardLayout tarjetasContenido = new CardLayout();
    private final JPanel pantallas = new JPanel(tarjetasContenido);
    private final JPanel navegacion = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
    private final JLabel usuario = Estilos.etiqueta("", Estilos.FUENTE_NEGRITA, Estilos.BLANCO);
    private final JButton cerrarSesion = Estilos.botonBarra("Cerrar sesión");
    private final JProgressBar progreso = new JProgressBar();
    private final JPanel bloqueo = new JPanel();
    private final Map<String, PanelBase> paneles = new HashMap<>();
    private final Map<String, Runnable> alAbrir = new HashMap<>();
    private final MenuPanel menu;
    private LoginPanel login;
    private boolean ocupado;

    public VentanaPrincipal(String urlServidor, BooleanSupplier servidorDisponible) {
        super(Estilos.TITULO_APLICACION);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1240, 760);
        setLocationRelativeTo(null);

        menu = new MenuPanel(this);
        JPanel aplicacion = new JPanel(new BorderLayout());
        aplicacion.add(barraSuperior(), BorderLayout.NORTH);
        pantallas.setBackground(Estilos.FONDO);
        aplicacion.add(pantallas, BorderLayout.CENTER);
        registrar(MENU, menu, null);

        raiz.add(aplicacion, TARJETA_APP);
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.add(raiz, BorderLayout.CENTER);
        IndicadorServidor indicador = new IndicadorServidor(servidorDisponible);
        cuerpo.add(barraInferior(indicador, urlServidor), BorderLayout.SOUTH);
        setContentPane(cuerpo);
        prepararBloqueo();
        indicador.iniciar();
    }

    private JComponent barraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilos.AZUL);
        barra.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 12));
        JButton marca = Estilos.botonBarra(Estilos.TITULO_APLICACION);
        marca.setFont(Estilos.FUENTE_SUBTITULO);
        marca.setToolTipText("Menú principal");
        marca.addActionListener(e -> mostrar(MENU));
        barra.add(marca, BorderLayout.WEST);
        navegacion.setOpaque(false);
        barra.add(navegacion, BorderLayout.CENTER);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(usuario);
        cerrarSesion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BLANCO),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        derecha.add(cerrarSesion);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private JComponent barraInferior(IndicadorServidor indicador, String urlServidor) {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setBackground(Estilos.BLANCO);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilos.BORDE),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        izquierda.setOpaque(false);
        izquierda.add(indicador);
        izquierda.add(Estilos.textoSuave(urlServidor));
        barra.add(izquierda, BorderLayout.WEST);
        progreso.setIndeterminate(true);
        progreso.setString("Procesando...");
        progreso.setStringPainted(true);
        progreso.setFont(Estilos.FUENTE_PEQUENA);
        progreso.setPreferredSize(new Dimension(180, 18));
        progreso.setVisible(false);
        barra.add(progreso, BorderLayout.EAST);
        return barra;
    }

    /** Capa transparente que bloquea el ratón mientras se procesa una petición (RNF-012). */
    private void prepararBloqueo() {
        bloqueo.setOpaque(false);
        bloqueo.addMouseListener(new MouseAdapter() {
        });
        bloqueo.addMouseMotionListener(new MouseAdapter() {
        });
        bloqueo.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setGlassPane(bloqueo);
    }

    // ------------------------------------------------------------------ armado (lo usa App)

    public void setLogin(LoginPanel login) {
        this.login = login;
        raiz.add(login, TARJETA_LOGIN);
        tarjetasRaiz.show(raiz, TARJETA_LOGIN);
    }

    /**
     * Agrega una pantalla con su botón en la barra superior y su tarjeta en el menú.
     *
     * @param accionAlAbrir acción del controlador que se ejecuta al entrar (puede ser null)
     */
    public void agregarPantalla(String clave, String nombre, String descripcion, PanelBase panel,
            Runnable accionAlAbrir) {
        registrar(clave, panel, accionAlAbrir);
        JButton boton = Estilos.botonBarra(nombre);
        boton.setFont(Estilos.FUENTE_PEQUENA.deriveFont(java.awt.Font.BOLD));
        boton.addActionListener(e -> mostrar(clave));
        navegacion.add(boton);
        menu.agregarOpcion(nombre, descripcion, () -> mostrar(clave));
    }

    public void setAccionCerrarSesion(Runnable accion) {
        cerrarSesion.addActionListener(e -> ejecutar(accion, menu));
        menu.agregarOpcion("Cerrar sesión", "Termina la sesión y vuelve al inicio.", () -> ejecutar(accion, menu));
    }

    private void registrar(String clave, PanelBase panel, Runnable accionAlAbrir) {
        paneles.put(clave, panel);
        if (accionAlAbrir != null) {
            alAbrir.put(clave, accionAlAbrir);
        }
        pantallas.add(panel, clave);
    }

    // ------------------------------------------------------------------ navegación

    /** Muestra una pantalla y ejecuta su acción de entrada (por ejemplo, cargar la tabla). */
    public void mostrar(String clave) {
        if (ocupado) {
            return;
        }
        PanelBase panel = paneles.get(clave);
        panel.limpiarAviso();
        tarjetasContenido.show(pantallas, clave);
        Runnable accion = alAbrir.get(clave);
        if (accion != null) {
            ejecutar(accion, panel);
        }
    }

    @Override
    public void mostrarMenu(String nombre) {
        Estilos.enEdt(() -> {
            usuario.setText(nombre);
            tarjetasRaiz.show(raiz, TARJETA_APP);
            menu.limpiarAviso();
            menu.saludar(nombre);
            tarjetasContenido.show(pantallas, MENU);
        });
    }

    @Override
    public int leerOpcion() {
        // En Swing cada opción del menú es un botón; no hay opción que leer.
        return -1;
    }

    @Override
    public void irAInicioSesion() {
        Estilos.enEdt(() -> irAInicioSesion(null));
    }

    /** Vuelve al inicio de sesión mostrando, si lo hay, el motivo (por ejemplo, sesión expirada). */
    void irAInicioSesion(String mensaje) {
        usuario.setText("");
        tarjetasRaiz.show(raiz, TARJETA_LOGIN);
        if (login != null) {
            login.mostrarFormulario();
            if (mensaje != null) {
                login.mostrarError(mensaje);
            }
        }
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        menu.mostrarMensaje(mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        menu.mostrarError(mensaje);
    }

    // ------------------------------------------------------------------ segundo plano

    /**
     * Ejecuta una acción del controlador fuera del hilo de Swing. Mientras dura, la barra de
     * progreso está visible y la ventana no acepta clics; un error inesperado se muestra en la
     * vista indicada y nunca cierra la aplicación.
     */
    void ejecutar(Runnable accion, VistaBase vistaError) {
        if (ocupado) {
            return;
        }
        setOcupado(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                accion.run();
                return null;
            }

            @Override
            protected void done() {
                setOcupado(false);
                try {
                    get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    LOG.log(Level.SEVERE, "Error inesperado en una acción", e.getCause());
                    vistaError.mostrarError(ERROR_INESPERADO);
                }
            }
        }.execute();
    }

    private void setOcupado(boolean valor) {
        ocupado = valor;
        progreso.setVisible(valor);
        bloqueo.setVisible(valor);
    }
}
