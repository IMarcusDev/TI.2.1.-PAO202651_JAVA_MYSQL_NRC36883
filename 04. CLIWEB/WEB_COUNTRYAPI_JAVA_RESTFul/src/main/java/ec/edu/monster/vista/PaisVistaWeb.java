package ec.edu.monster.vista;

import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.Pais;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Gestión de países (ECUD4) en paises.jsp.
 *
 * <p>Cómo encaja el ciclo del controlador (pedir datos, validar, reabrir el formulario si hay
 * error) con una petición web: la primera llamada a {@link #leerPais(Pais)} devuelve lo que
 * el usuario envió; si el controlador la vuelve a llamar es porque hubo un error, y entonces
 * la vista deja el formulario pendiente (con lo escrito) y devuelve null, como si el usuario
 * hubiera cerrado el diálogo. paises.jsp lo muestra abierto con el mensaje.</p>
 *
 * @author Escobar Marcos
 */
public class PaisVistaWeb extends VistaWeb implements PaisVista {

    /** Lista de países de la tabla. */
    public static final String PAISES = "paises";
    /** País del formulario abierto (nuevo, en edición o devuelto con error). */
    public static final String FORMULARIO = "formulario";
    /** País que se va a eliminar (panel de confirmación). */
    public static final String ELIMINAR = "eliminar";

    /** Última tabla mostrada a este usuario: de aquí sale el país elegido por su ID. */
    private List<Pais> ultimaTabla = List.of();
    private boolean datosLeidos;

    @Override
    public void atender(HttpServletRequest peticion) {
        super.atender(peticion);
        datosLeidos = false;
    }

    public List<Pais> getUltimaTabla() {
        return ultimaTabla;
    }

    /** País de la última tabla con ese ID, o null. */
    public Pais buscar(String id) {
        try {
            int numero = Integer.parseInt(id);
            return ultimaTabla.stream().filter(p -> p.getId() == numero).findFirst().orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ PaisVista

    @Override
    public void mostrarTabla(List<Pais> paises) {
        ultimaTabla = paises;
        poner(PAISES, paises);
    }

    @Override
    public String leerFiltro() {
        return parametro("q");
    }

    @Override
    public Pais leerPais(Pais inicial) {
        if (!datosLeidos) {
            datosLeidos = true;
            return leerFormulario();
        }
        // Segunda llamada: el controlador reabre el formulario por un error de validación,
        // 400 o 409. Se devuelve a la página con lo que el usuario escribió.
        poner(FORMULARIO, inicial);
        return null;
    }

    @Override
    public Pais paisSeleccionado() {
        return buscar(parametro("id"));
    }

    @Override
    public boolean confirmarEliminacion(Pais pais) {
        return "si".equals(parametro("confirmado"));
    }

    /** Arma el país con los campos del formulario, como FormularioPaisDialogo en escritorio. */
    private Pais leerFormulario() {
        String nombre = parametro("nombre");
        String poblacion = parametro("poblacion");
        return new Pais(
                nombre == null ? "" : nombre,
                parametroConValor("capital"),
                parametroConValor("region"),
                parametroConValor("subregion"),
                ValidadorDatos.leerPoblacion(poblacion == null ? "" : poblacion),
                ValidadorDatos.leerArea(parametro("area")),
                parametroConValor("moneda"));
    }
}
