package ec.edu.monster.vista;

import ec.edu.monster.modelos.Pais;
import java.util.List;

/**
 * «Consultar país en la API» (ECUD3) en consultar.jsp. Los resultados quedan guardados para
 * este usuario: así «Guardar» y la recarga de la página no vuelven a gastar una consulta a
 * REST Countries.
 *
 * @author Escobar Marcos
 */
public class ConsultaApiVistaWeb extends VistaWeb implements ConsultaApiVista {

    public static final String RESULTADOS = "resultados";

    private List<Pais> resultados = List.of();
    private String ultimoNombre = "";

    public List<Pais> getResultados() {
        return resultados;
    }

    public String getUltimoNombre() {
        return ultimoNombre;
    }

    @Override
    public String leerNombre() {
        String nombre = parametro("nombre");
        ultimoNombre = nombre == null ? "" : nombre;
        resultados = List.of();
        return nombre;
    }

    @Override
    public void mostrarResultados(List<Pais> paises) {
        resultados = paises;
        poner(RESULTADOS, paises);
    }

    /** El N° (desde 1) de la fila elegida, enviado por el botón «Guardar» de esa fila. */
    @Override
    public Pais leerSeleccion() {
        try {
            int numero = Integer.parseInt(parametro("numero"));
            return numero >= 1 && numero <= resultados.size() ? resultados.get(numero - 1) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
