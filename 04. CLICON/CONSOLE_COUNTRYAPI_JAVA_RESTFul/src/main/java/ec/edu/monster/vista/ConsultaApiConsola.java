package ec.edu.monster.vista;

import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.servicios.SesionServicio;
import java.util.List;

/**
 * «Consultar país en la API» (ECUD3): busca en REST Countries a través del servidor, muestra si
 * cada resultado ya está guardado y permite guardar uno por su número (N°).
 *
 * @author Escobar Marcos
 */
public class ConsultaApiConsola extends PantallaConsola implements ConsultaApiVista {

    private ApiControlador controlador;
    private List<Pais> resultados;
    private Pais seleccion;

    public ConsultaApiConsola(Consola consola, SesionServicio sesion) {
        super(consola, sesion);
    }

    public void setControlador(ApiControlador controlador) {
        this.controlador = controlador;
    }

    /** Pantalla de consulta: consulta, ofrece guardar y pregunta si se consulta otro país. */
    public void mostrar() {
        consola.titulo("Consultar país en la API");
        consola.ayuda("Consulta REST Countries por nombre (por ejemplo «ecuador») y guarde el país elegido.");
        do {
            resultados = null;
            ejecutar(controlador::consultar);
            if (sesion.estaActiva() && resultados != null && !resultados.isEmpty()) {
                String numero = consola.leer("N° del país a guardar (Enter para no guardar)");
                if (!numero.isEmpty()) {
                    seleccion = porNumero(numero);
                    ejecutar(controlador::guardar);
                }
            }
        } while (sesion.estaActiva() && consola.confirmar("¿Consultar otro país?"));
    }

    private Pais porNumero(String texto) {
        try {
            int numero = Integer.parseInt(texto);
            return numero >= 1 && numero <= resultados.size() ? resultados.get(numero - 1) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ ConsultaApiVista

    @Override
    public String leerNombre() {
        return consola.leer("Nombre del país");
    }

    @Override
    public void mostrarResultados(List<Pais> paises) {
        resultados = paises;
        Tabla tabla = new Tabla("N°", "Nombre", "Capital", "Región", "Población", "Área (km²)", "Moneda", "¿Guardado?")
                .derecha(0, 4, 5)
                .maximo(1, 24).maximo(2, 16).maximo(6, 26);
        for (int i = 0; i < paises.size(); i++) {
            Pais pais = paises.get(i);
            tabla.fila(i + 1, pais.getNombre(), pais.getCapital(), pais.getRegion(), Tabla.entero(pais.getPoblacion()),
                    Tabla.decimal(pais.getArea()), pais.getMoneda(), pais.isGuardado() ? "Sí" : "No");
        }
        consola.tabla(tabla);
    }

    @Override
    public Pais leerSeleccion() {
        return seleccion;
    }
}
