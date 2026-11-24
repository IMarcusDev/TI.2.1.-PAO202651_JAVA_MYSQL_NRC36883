package ec.edu.monster.vista;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Vista web (equivale al PanelBase de escritorio). Cada usuario tiene sus propias vistas, que
 * se enlazan con la petición en curso mientras el controlador trabaja: lo que el controlador
 * «muestra» queda como atributo de la petición y la JSP lo dibuja después.
 *
 * <ul>
 *   <li>{@code mensaje}: aviso verde;</li>
 *   <li>{@code error}: aviso rojo.</li>
 * </ul>
 *
 * @author Escobar Marcos
 */
public abstract class VistaWeb implements VistaBase {

    public static final String MENSAJE = "mensaje";
    public static final String ERROR = "error";

    private HttpServletRequest peticion;

    /** Enlaza la vista con la petición que se está atendiendo. */
    public void atender(HttpServletRequest peticion) {
        this.peticion = peticion;
    }

    /** Suelta la petición al terminar. */
    public void terminar() {
        this.peticion = null;
    }

    protected HttpServletRequest peticion() {
        return peticion;
    }

    /** Parámetro del formulario sin espacios a los lados, o null si no se envió. */
    protected String parametro(String nombre) {
        String valor = peticion.getParameter(nombre);
        return valor == null ? null : valor.trim();
    }

    /** Parámetro sin espacios, o null si no se envió o está vacío. */
    protected String parametroConValor(String nombre) {
        String valor = parametro(nombre);
        return valor == null || valor.isEmpty() ? null : valor;
    }

    protected void poner(String atributo, Object valor) {
        peticion.setAttribute(atributo, valor);
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        poner(MENSAJE, mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        poner(ERROR, mensaje);
    }

    /**
     * El controlador ya cerró la sesión: el servlet lo detecta con {@code estaActiva()} y
     * redirige al inicio de sesión con el mensaje de error.
     */
    @Override
    public void irAInicioSesion() {
        // Nada que hacer aquí: lo resuelve el servlet al terminar la acción.
    }
}
