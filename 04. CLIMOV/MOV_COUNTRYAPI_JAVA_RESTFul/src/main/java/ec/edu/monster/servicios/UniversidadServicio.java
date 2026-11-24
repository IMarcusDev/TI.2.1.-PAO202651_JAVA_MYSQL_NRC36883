package ec.edu.monster.servicios;

import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.modelos.ResumenImportacion;
import ec.edu.monster.modelos.Universidad;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Universidades: importar desde la API (vía el servidor) y listar las guardadas.
 *
 * @author Escobar Marcos
 */
public class UniversidadServicio {

    private static final Type LISTA = TypeToken.getParameterized(List.class, Universidad.class).getType();

    private final ClienteRest cliente;

    public UniversidadServicio(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public RespuestaDTO<ResumenImportacion> importar(String token, String pais) {
        return cliente.enviar("POST", "/Universidades/importar?pais=" + ClienteRest.codificar(pais.trim()),
                token, null, ResumenImportacion.class);
    }

    public RespuestaDTO<List<Universidad>> listar(String token, String pais) {
        return cliente.enviar("GET", "/Universidades?pais=" + ClienteRest.codificar(pais.trim()), token, null, LISTA);
    }
}
