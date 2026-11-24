package ec.edu.monster.vista;

import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.modelos.Universidad;
import java.util.List;

/**
 * Universidades (ECUD10) en universidades.jsp: importar las de un país (en inglés) y ver las
 * guardadas. Sin país en la dirección se muestran las de Ecuador, como en escritorio.
 *
 * @author Escobar Marcos
 */
public class UniversidadVistaWeb extends VistaWeb implements UniversidadVista {

    public static final String UNIVERSIDADES = "universidades";
    public static final String PAIS = "pais";
    public static final String PAIS_POR_DEFECTO = "Ecuador";

    @Override
    public String leerPais() {
        String pais = parametro(PAIS);
        String valor = pais == null ? PAIS_POR_DEFECTO : pais;
        poner(PAIS, valor);
        return valor;
    }

    @Override
    public void mostrarResumen(ResumenImportacion resumen) {
        mostrarMensaje("Importación de universidades finalizada: " + resumen.getRecibidos() + " recibidas, "
                + resumen.getRegistrados() + " registradas y " + resumen.getActualizados() + " actualizadas");
    }

    @Override
    public void mostrarUniversidades(List<Universidad> universidades) {
        poner(UNIVERSIDADES, universidades);
    }
}
