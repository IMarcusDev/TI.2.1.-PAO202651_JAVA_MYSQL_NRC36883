package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.servicios.SesionServicio;
import java.util.ArrayList;
import java.util.List;

/**
 * «Buscar y filtrar» (ECUD5): nombre y capital parciales y una región (o todas). Cada búsqueda
 * consulta de nuevo al servidor.
 *
 * @author Escobar Marcos
 */
public class BusquedaConsola extends PantallaConsola implements BusquedaVista {

    private PaisControlador controlador;
    private List<String> regiones = new ArrayList<>();

    public BusquedaConsola(Consola consola, SesionServicio sesion) {
        super(consola, sesion);
    }

    public void setControlador(PaisControlador controlador) {
        this.controlador = controlador;
    }

    /** Pantalla «Buscar y filtrar»: busca hasta que el usuario no quiera otra búsqueda. */
    public void mostrar() {
        consola.titulo("Buscar y filtrar");
        consola.ayuda("Combine nombre, capital y región. Enter en un criterio significa «cualquiera».");
        do {
            ejecutar(controlador::buscarYFiltrar);
        } while (sesion.estaActiva() && consola.confirmar("¿Hacer otra búsqueda?"));
    }

    // ------------------------------------------------------------------ BusquedaVista

    @Override
    public CriteriosBusqueda leerCriterios() {
        String nombre = consola.leer("Nombre");
        String capital = consola.leer("Capital");
        return new CriteriosBusqueda(vacioANulo(nombre), vacioANulo(capital), leerRegion());
    }

    @Override
    public void mostrarFiltrosRegion(List<String> regiones) {
        this.regiones = new ArrayList<>(regiones);
    }

    @Override
    public void mostrarResultados(List<Pais> paises, int total) {
        if (!paises.isEmpty()) {
            consola.tabla(PaisConsola.tablaPaises(paises));
        }
        consola.subtitulo(total == 1 ? "1 país encontrado" : total + " países encontrados");
    }

    /** 0 o Enter: todas las regiones; 1..n: una región. Repite la pregunta si el número no existe. */
    private String leerRegion() {
        StringBuilder opciones = new StringBuilder("Región:  0. Todas");
        for (int i = 0; i < regiones.size(); i++) {
            opciones.append("  ").append(i + 1).append(". ").append(regiones.get(i));
        }
        while (true) {
            consola.ayuda(opciones.toString());
            String texto = consola.leer("Región [0]");
            if (texto.isEmpty() || "0".equals(texto)) {
                return null;
            }
            try {
                int numero = Integer.parseInt(texto);
                if (numero >= 1 && numero <= regiones.size()) {
                    return regiones.get(numero - 1);
                }
            } catch (NumberFormatException e) {
                // Se vuelve a preguntar.
            }
            consola.error(OPCION_NO_VALIDA);
        }
    }

    private static String vacioANulo(String texto) {
        return texto == null || texto.isEmpty() ? null : texto;
    }
}
