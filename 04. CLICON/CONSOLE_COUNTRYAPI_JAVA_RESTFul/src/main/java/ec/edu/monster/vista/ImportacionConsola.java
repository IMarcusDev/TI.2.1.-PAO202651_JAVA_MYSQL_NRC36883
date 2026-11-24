package ec.edu.monster.vista;

import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.servicios.SesionServicio;

/**
 * «Importar países» (ECUD2): pide confirmación, importa todos los países de REST Countries a
 * través del servidor y muestra cuántos se recibieron, registraron y actualizaron.
 *
 * @author Escobar Marcos
 */
public class ImportacionConsola extends PantallaConsola implements ImportacionVista {

    private ApiControlador controlador;

    public ImportacionConsola(Consola consola, SesionServicio sesion) {
        super(consola, sesion);
    }

    public void setControlador(ApiControlador controlador) {
        this.controlador = controlador;
    }

    public void mostrar() {
        consola.titulo("Importar países");
        consola.ayuda("Trae todos los países de la API REST Countries. Los nuevos se registran y los existentes "
                + "se actualizan, sin duplicar.");
        ejecutar(controlador::importar);
    }

    @Override
    public boolean confirmarImportacion() {
        boolean confirmado = consola.confirmar(
                "¿Importar los países desde la API REST Countries? Los países existentes se actualizarán");
        if (confirmado) {
            consola.ayuda("Procesando...");
        }
        return confirmado;
    }

    @Override
    public void mostrarResumen(ResumenImportacion resumen) {
        consola.tabla(new Tabla("Recibidos de la API", "Registrados (nuevos)", "Actualizados")
                .derecha(0, 1, 2)
                .fila(Tabla.entero(resumen.getRecibidos()), Tabla.entero(resumen.getRegistrados()),
                        Tabla.entero(resumen.getActualizados())));
        consola.mensaje("Importación finalizada: " + resumen.getRecibidos() + " países recibidos, "
                + resumen.getRegistrados() + " registrados y " + resumen.getActualizados() + " actualizados");
    }
}
