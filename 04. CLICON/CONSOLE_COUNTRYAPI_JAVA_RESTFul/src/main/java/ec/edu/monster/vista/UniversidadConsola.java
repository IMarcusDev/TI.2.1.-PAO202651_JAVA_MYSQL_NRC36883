package ec.edu.monster.vista;

import ec.edu.monster.controlador.UniversidadControlador;
import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.modelos.Universidad;
import ec.edu.monster.servicios.SesionServicio;
import java.util.List;

/**
 * Universidades (ECUD10): importa desde la API de universidades las de un país (en inglés) y
 * muestra las guardadas. El país propuesto es el último escrito («Ecuador» al inicio).
 *
 * @author Escobar Marcos
 */
public class UniversidadConsola extends PantallaConsola implements UniversidadVista {

    private UniversidadControlador controlador;
    private String ultimoPais = "Ecuador";

    public UniversidadConsola(Consola consola, SesionServicio sesion) {
        super(consola, sesion);
    }

    public void setControlador(UniversidadControlador controlador) {
        this.controlador = controlador;
    }

    /** Pantalla «Universidades»; vuelve al menú con 0 o si la sesión expira. */
    public void mostrar() {
        consola.titulo("Universidades");
        consola.ayuda("Importa desde la API de universidades las de un país (escrito en inglés) y consulta las guardadas.");
        while (sesion.estaActiva()) {
            consola.lineaEnBlanco();
            consola.texto("1. Importar   2. Ver guardadas   0. Volver");
            switch (consola.leerOpcion("Opción")) {
                case 1 -> ejecutar(controlador::importar);
                case 2 -> ejecutar(controlador::listar);
                case 0 -> {
                    return;
                }
                default -> consola.error(OPCION_NO_VALIDA);
            }
        }
    }

    // ------------------------------------------------------------------ UniversidadVista

    @Override
    public String leerPais() {
        String texto = consola.leer("País en inglés [" + ultimoPais + "]");
        if (!texto.isEmpty()) {
            ultimoPais = texto;
        }
        return ultimoPais;
    }

    @Override
    public void mostrarResumen(ResumenImportacion resumen) {
        consola.mensaje("Importación de universidades finalizada: " + resumen.getRecibidos() + " recibidas, "
                + resumen.getRegistrados() + " registradas y " + resumen.getActualizados() + " actualizadas");
    }

    @Override
    public void mostrarUniversidades(List<Universidad> universidades) {
        if (!universidades.isEmpty()) {
            Tabla tabla = new Tabla("Nombre", "Dominio", "Sitio web").maximo(0, 50).maximo(1, 24).maximo(2, 38);
            for (Universidad universidad : universidades) {
                tabla.fila(universidad.getNombre(), universidad.getDominio(), universidad.getSitioWeb());
            }
            consola.tabla(tabla);
        }
        consola.ayuda(universidades.size() == 1 ? "1 universidad" : universidades.size() + " universidades");
    }
}
