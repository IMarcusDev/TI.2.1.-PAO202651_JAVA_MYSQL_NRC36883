package ec.edu.monster.vista;

import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.modelos.ConteoRegion;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.PromedioRegion;
import ec.edu.monster.modelos.Reporte;
import ec.edu.monster.servicios.SesionServicio;

/**
 * Reportes (ECUD6): país más poblado, cantidad de países por región y promedio poblacional por
 * región.
 *
 * @author Escobar Marcos
 */
public class ReporteConsola extends PantallaConsola implements ReporteVista {

    private ReporteControlador controlador;

    public ReporteConsola(Consola consola, SesionServicio sesion) {
        super(consola, sesion);
    }

    public void setControlador(ReporteControlador controlador) {
        this.controlador = controlador;
    }

    public void mostrar() {
        consola.titulo("Reportes");
        consola.ayuda("Estadísticas calculadas sobre los países almacenados.");
        ejecutar(controlador::generar);
    }

    @Override
    public void mostrarReporte(Reporte reporte) {
        consola.subtitulo("País más poblado");
        Pais pais = reporte.getPaisMasPoblado();
        consola.texto(pais == null ? "—"
                : pais.getNombre() + " · " + Tabla.entero(pais.getPoblacion()) + " habitantes · " + pais.getRegion());

        consola.subtitulo("Cantidad de países por región");
        Tabla conteo = new Tabla("Región", "Países").derecha(1);
        for (ConteoRegion fila : reporte.getPaisesPorRegion()) {
            conteo.fila(fila.getRegion(), Tabla.entero(fila.getCantidad()));
        }
        consola.tabla(conteo);

        consola.subtitulo("Promedio poblacional por región");
        Tabla promedio = new Tabla("Región", "Promedio de población").derecha(1);
        for (PromedioRegion fila : reporte.getPromedioPorRegion()) {
            promedio.fila(fila.getRegion(), Tabla.entero(fila.getPromedioPoblacion()));
        }
        consola.tabla(promedio);
    }
}
