package ec.edu.monster.vista;

import ec.edu.monster.modelos.Pais;
import java.util.List;

/**
 * Tabla de países con búsqueda por nombre y botones Nuevo, Editar y Eliminar (ECUD4).
 *
 * @author Escobar Marcos
 */
public interface PaisVista extends VistaBase {

    void mostrarTabla(List<Pais> paises);

    /** Texto del campo «Buscar». */
    String leerFiltro();

    /**
     * Pide los siete datos del país en un formulario.
     *
     * @param inicial datos con los que se abre el formulario (null para uno nuevo)
     * @return el país escrito, o null si el usuario canceló. Si la población o el área no son
     *         números válidos, se devuelven como -1 y NaN para que el validador los rechace.
     */
    Pais leerPais(Pais inicial);

    /** País elegido en la tabla, o null si no hay ninguno. */
    Pais paisSeleccionado();

    /** Pide confirmar «¿Eliminar el país &lt;nombre&gt;?». */
    boolean confirmarEliminacion(Pais pais);
}
