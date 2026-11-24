package ec.edu.monster.vista;

import ec.edu.monster.modelos.Pais;
import java.util.List;

/**
 * «Consultar país en la API»: nombre, resultados y botón Guardar (ECUD3).
 *
 * @author Escobar Marcos
 */
public interface ConsultaApiVista extends VistaBase {

    String leerNombre();

    void mostrarResultados(List<Pais> paises);

    /** País elegido en los resultados, o null si no hay ninguno. */
    Pais leerSeleccion();
}
