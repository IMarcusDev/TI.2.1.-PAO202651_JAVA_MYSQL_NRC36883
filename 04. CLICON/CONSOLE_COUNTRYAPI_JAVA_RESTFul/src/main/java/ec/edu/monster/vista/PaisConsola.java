package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.servicios.SesionServicio;
import java.util.List;
import java.util.Locale;

/**
 * Gestión de países (ECUD4): al entrar lista los países y ofrece buscar por nombre, registrar,
 * editar y eliminar. Para editar o eliminar se escribe el ID que aparece en la tabla.
 *
 * @author Escobar Marcos
 */
public class PaisConsola extends PantallaConsola implements PaisVista {

    private PaisControlador controlador;
    /** Última tabla mostrada: de aquí se toma el país por su ID. */
    private List<Pais> ultimaTabla = List.of();

    public PaisConsola(Consola consola, SesionServicio sesion) {
        super(consola, sesion);
    }

    public void setControlador(PaisControlador controlador) {
        this.controlador = controlador;
    }

    /** Pantalla «Países»; vuelve al menú con 0 o si la sesión expira. */
    public void mostrar() {
        consola.titulo("Países");
        ejecutar(controlador::listar);
        while (sesion.estaActiva()) {
            consola.lineaEnBlanco();
            consola.texto("1. Listar todos   2. Buscar por nombre   3. Nuevo   4. Editar   5. Eliminar   0. Volver");
            switch (consola.leerOpcion("Opción")) {
                case 1 -> ejecutar(controlador::listar);
                case 2 -> ejecutar(controlador::buscar);
                case 3 -> ejecutar(controlador::registrar);
                case 4 -> ejecutar(controlador::actualizar);
                case 5 -> ejecutar(controlador::eliminar);
                case 0 -> {
                    return;
                }
                default -> consola.error(OPCION_NO_VALIDA);
            }
        }
    }

    /** Tabla de países que comparten Países y Buscar y filtrar (cabe en 120 columnas). */
    static Tabla tablaPaises(List<Pais> paises) {
        Tabla tabla = new Tabla("ID", "Nombre", "Capital", "Región", "Subregión", "Población", "Área (km²)", "Moneda")
                .derecha(0, 5, 6)
                .maximo(1, 18).maximo(2, 12).maximo(3, 9).maximo(4, 14).maximo(7, 14);
        for (Pais pais : paises) {
            tabla.fila(pais.getId(), pais.getNombre(), pais.getCapital(), pais.getRegion(), pais.getSubregion(),
                    Tabla.entero(pais.getPoblacion()), Tabla.decimal(pais.getArea()), pais.getMoneda());
        }
        return tabla;
    }

    static String totalPaises(int cantidad) {
        return cantidad == 1 ? "1 país" : cantidad + " países";
    }

    // ------------------------------------------------------------------ PaisVista

    @Override
    public void mostrarTabla(List<Pais> paises) {
        ultimaTabla = paises;
        if (paises.isEmpty()) {
            return;
        }
        consola.tabla(tablaPaises(paises));
        consola.ayuda(totalPaises(paises.size()));
    }

    @Override
    public String leerFiltro() {
        return consola.leer("Nombre o parte del nombre");
    }

    /**
     * Pide los datos campo por campo. Enter conserva el valor entre corchetes y «-» deja vacío
     * un campo opcional. Devuelve null si el usuario no confirma.
     */
    @Override
    public Pais leerPais(Pais inicial) {
        boolean nuevo = inicial == null || inicial.getId() == 0;
        consola.subtitulo(nuevo ? "Nuevo país" : "Datos del país");
        consola.ayuda("Enter conserva el valor entre corchetes; «-» deja vacío un campo opcional.");
        Pais base = inicial == null ? new Pais() : inicial;
        String nombre = campo("Nombre *", base.getNombre());
        String capital = campo("Capital", base.getCapital());
        String region = leerRegion(base.getRegion());
        String subregion = campo("Subregión", base.getSubregion());
        // Un valor inválido escrito antes (-1 / NaN) se vuelve a pedir sin valor por defecto.
        String poblacion = campo("Población *",
                inicial == null || base.getPoblacion() < 0 ? null : Long.toString(base.getPoblacion()));
        String area = campo("Área (km²)",
                inicial == null || Double.isNaN(base.getArea()) || base.getArea() < 0 ? null : editable(base.getArea()));
        String moneda = campo("Moneda", base.getMoneda());
        if (!consola.confirmar("¿Guardar los datos?")) {
            return null;
        }
        return new Pais(nombre == null ? "" : nombre, capital, region, subregion,
                ValidadorDatos.leerPoblacion(poblacion == null ? "" : poblacion),
                ValidadorDatos.leerArea(area), moneda);
    }

    @Override
    public Pais paisSeleccionado() {
        String texto = consola.leer("ID del país (columna ID de la tabla)");
        try {
            int id = Integer.parseInt(texto);
            return ultimaTabla.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean confirmarEliminacion(Pais pais) {
        return consola.confirmar("¿Eliminar el país " + pais.getNombre() + "?");
    }

    // ------------------------------------------------------------------ apoyo del formulario

    /** Lee un campo mostrando el valor actual; Enter lo conserva y «-» lo deja vacío. */
    private String campo(String etiqueta, String actual) {
        boolean hayActual = actual != null && !actual.isEmpty();
        String texto = consola.leer(hayActual ? etiqueta + " [" + actual + "]" : etiqueta);
        if (texto.isEmpty()) {
            return hayActual ? actual : null;
        }
        return "-".equals(texto) ? null : texto;
    }

    /** Región por número o por nombre; Enter conserva la actual. Una respuesta inválida queda vacía. */
    private String leerRegion(String actual) {
        StringBuilder opciones = new StringBuilder("Regiones:");
        for (int i = 0; i < Pais.REGIONES.size(); i++) {
            opciones.append("  ").append(i + 1).append(". ").append(Pais.REGIONES.get(i));
        }
        consola.ayuda(opciones.toString());
        String elegida = campo("Región *", actual);
        return elegida == null ? null : region(elegida);
    }

    /** «2» o «europa» → «Europa»; cualquier otra cosa → null (ValidadorDatos pide elegir una). */
    static String region(String texto) {
        try {
            int numero = Integer.parseInt(texto);
            return numero >= 1 && numero <= Pais.REGIONES.size() ? Pais.REGIONES.get(numero - 1) : null;
        } catch (NumberFormatException e) {
            String buscada = texto.toLowerCase(Locale.ROOT);
            return Pais.REGIONES.stream().filter(r -> r.toLowerCase(Locale.ROOT).equals(buscada)).findFirst()
                    .orElse(null);
        }
    }

    /** 276841.5 → «276841,5»; 276841.0 → «276841». */
    private static String editable(double valor) {
        if (valor == Math.rint(valor)) {
            return Long.toString((long) valor);
        }
        return Double.toString(valor).replace('.', ',');
    }
}
