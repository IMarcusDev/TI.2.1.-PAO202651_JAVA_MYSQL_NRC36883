package ec.edu.monster.vista;

import ec.edu.monster.modelos.LoginRequest;
import ec.edu.monster.servicios.SesionServicio;

/**
 * Inicio de sesión (ECUD1). La clave no se muestra al escribirla cuando hay una terminal real.
 * Escribir 0 como usuario termina el programa.
 *
 * @author Escobar Marcos
 */
public class LoginConsola extends PantallaConsola implements LoginVista {

    static final String SALIR = "0";

    private boolean formularioMostrado;

    public LoginConsola(Consola consola, SesionServicio sesion) {
        super(consola, sesion);
    }

    @Override
    public void mostrarFormulario() {
        formularioMostrado = true;
        consola.titulo("CountryAPI · Iniciar sesión");
        consola.ayuda("Consumo de APIs públicas · Escriba 0 como usuario para salir.");
    }

    @Override
    public LoginRequest leerCredenciales() {
        String usuario = consola.leer("Usuario");
        if (SALIR.equals(usuario)) {
            throw new Consola.Salida();
        }
        return new LoginRequest(usuario, consola.leerClave("Clave"));
    }

    @Override
    public void limpiarClave() {
        // En consola la clave no queda escrita en ningún campo: el próximo intento la vuelve a pedir.
    }

    /**
     * True si el formulario ya se mostró desde la última consulta (por ejemplo, al cerrar
     * sesión lo muestra el controlador); así App no lo repite.
     */
    public boolean tomarFormularioMostrado() {
        boolean mostrado = formularioMostrado;
        formularioMostrado = false;
        return mostrado;
    }
}
