package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelos.LoginRequest;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Inicio de sesión (ECUD1): usuario, clave oculta y botón «Ingresar» (también con Enter).
 *
 * @author Escobar Marcos
 */
public final class LoginPanel extends JPanel implements LoginVista {

    private final VentanaPrincipal ventana;
    private final JTextField usuario = Estilos.campo(22);
    private final JPasswordField clave = Estilos.estiloEntrada(new JPasswordField(22));
    private final JButton ingresar = Estilos.botonPrimario("Ingresar");
    private final Alerta alerta = new Alerta();

    public LoginPanel(VentanaPrincipal ventana) {
        super(new GridBagLayout());
        this.ventana = ventana;
        setBackground(Estilos.FONDO);

        JPanel tarjeta = Estilos.tarjeta();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)));
        tarjeta.add(alinear(Estilos.titulo(Estilos.TITULO_APLICACION)));
        tarjeta.add(alinear(Estilos.textoSuave("Consumo de APIs públicas · Inicie sesión para continuar")));
        tarjeta.add(Box.createVerticalStrut(20));
        tarjeta.add(alinear(Estilos.etiqueta("Usuario", Estilos.FUENTE_NEGRITA, Estilos.TEXTO)));
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(alinear(usuario));
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(alinear(Estilos.etiqueta("Clave", Estilos.FUENTE_NEGRITA, Estilos.TEXTO)));
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(alinear(clave));
        tarjeta.add(Box.createVerticalStrut(16));
        alerta.setMaximumSize(new Dimension(360, 120));
        tarjeta.add(alinear(alerta));
        tarjeta.add(Box.createVerticalStrut(8));
        ingresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tarjeta.add(alinear(ingresar));
        tarjeta.setPreferredSize(new Dimension(420, tarjeta.getPreferredSize().height + 40));
        add(tarjeta);

        usuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        clave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    }

    private static Component alinear(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }

    /** Conecta el botón «Ingresar» y la tecla Enter con el controlador. */
    public void setControlador(LoginControlador controlador) {
        Runnable accion = () -> {
            alerta.ocultar();
            ventana.ejecutar(controlador::iniciarSesion, this);
        };
        ingresar.addActionListener(e -> accion.run());
        usuario.addActionListener(e -> clave.requestFocusInWindow());
        clave.addActionListener(e -> accion.run());
    }

    @Override
    public void mostrarFormulario() {
        Estilos.enEdt(() -> {
            usuario.setText("");
            clave.setText("");
            alerta.ocultar();
            usuario.requestFocusInWindow();
        });
    }

    @Override
    public LoginRequest leerCredenciales() {
        return Estilos.enEdtYEsperar(() -> new LoginRequest(usuario.getText(), new String(clave.getPassword())));
    }

    @Override
    public void limpiarClave() {
        Estilos.enEdt(() -> {
            clave.setText("");
            clave.requestFocusInWindow();
        });
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        Estilos.enEdt(() -> alerta.exito(mensaje));
    }

    @Override
    public void mostrarError(String mensaje) {
        Estilos.enEdt(() -> alerta.error(mensaje));
    }

    @Override
    public void irAInicioSesion() {
        // Ya está en el inicio de sesión.
    }
}
