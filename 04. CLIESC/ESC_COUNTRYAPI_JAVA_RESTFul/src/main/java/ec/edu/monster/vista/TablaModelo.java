package ec.edu.monster.vista;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;

/**
 * Modelo de JTable de solo lectura para una lista de objetos: cada columna tiene un título y una
 * función que saca el valor de la fila.
 *
 * @param <T> tipo de las filas
 * @author Escobar Marcos
 */
public final class TablaModelo<T> extends AbstractTableModel {

    /** Una columna: título, cómo obtener su valor y si es numérica (se alinea a la derecha). */
    public record Columna<T>(String titulo, Function<T, Object> valor, boolean numerica) {

        public static <T> Columna<T> texto(String titulo, Function<T, Object> valor) {
            return new Columna<>(titulo, valor, false);
        }

        public static <T> Columna<T> numero(String titulo, Function<T, Object> valor) {
            return new Columna<>(titulo, valor, true);
        }
    }

    private final List<Columna<T>> columnas;
    private List<T> filas = new ArrayList<>();

    @SafeVarargs
    public TablaModelo(Columna<T>... columnas) {
        this.columnas = List.of(columnas);
    }

    public void setFilas(List<T> filas) {
        this.filas = new ArrayList<>(filas);
        fireTableDataChanged();
    }

    public List<T> getFilas() {
        return filas;
    }

    public T fila(int indice) {
        return indice < 0 || indice >= filas.size() ? null : filas.get(indice);
    }

    public boolean esNumerica(int columna) {
        return columna >= 0 && columna < columnas.size() && columnas.get(columna).numerica();
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.size();
    }

    @Override
    public String getColumnName(int columna) {
        return columnas.get(columna).titulo();
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        // Comparable: el ordenamiento de las columnas numéricas compara números (no texto) y la
        // celda usa el renderizador común de Estilos.tabla, que les da formato con miles.
        return columnas.get(columna).numerica() ? Comparable.class : String.class;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return columnas.get(columna).valor().apply(filas.get(fila));
    }
}
