package ec.edu.monster.servicios;

import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelos.CriteriosBusqueda;
import ec.edu.monster.modelos.Pais;
import ec.edu.monster.modelos.RespuestaDTO;
import ec.edu.monster.modelos.ResumenImportacion;
import java.lang.reflect.Type;
import java.util.List;
import java.util.StringJoiner;

/**
 * Operaciones de países: CRUD, búsqueda, importación y consulta en la API (/Paises).
 *
 * @author Escobar Marcos
 */
public class PaisServicio {

    private static final Type LISTA_PAISES = TypeToken.getParameterized(List.class, Pais.class).getType();

    private final ClienteRest cliente;

    public PaisServicio(ClienteRest cliente) {
        this.cliente = cliente;
    }

    /** Países guardados; con nombre, solo los que lo contienen. */
    public RespuestaDTO<List<Pais>> listar(String token, String nombre) {
        String ruta = nombre == null || nombre.isBlank() ? "/Paises" : "/Paises?nombre=" + ClienteRest.codificar(nombre.trim());
        return cliente.enviar("GET", ruta, token, null, LISTA_PAISES);
    }

    public RespuestaDTO<Pais> registrar(String token, Pais pais) {
        return cliente.enviar("POST", "/Paises", token, pais, Pais.class);
    }

    public RespuestaDTO<Pais> actualizar(String token, int id, Pais pais) {
        return cliente.enviar("PUT", "/Paises/" + id, token, pais, Pais.class);
    }

    public RespuestaDTO<Void> eliminar(String token, int id) {
        return cliente.enviar("DELETE", "/Paises/" + id, token, null, Void.class);
    }

    /** GET /Paises/buscar con los criterios que tienen valor. */
    public RespuestaDTO<List<Pais>> buscar(String token, CriteriosBusqueda criterios) {
        StringJoiner query = new StringJoiner("&", "?", "").setEmptyValue("");
        agregar(query, "nombre", criterios.getNombre());
        agregar(query, "capital", criterios.getCapital());
        agregar(query, "region", criterios.getRegion());
        return cliente.enviar("GET", "/Paises/buscar" + query, token, null, LISTA_PAISES);
    }

    public RespuestaDTO<ResumenImportacion> importar(String token) {
        return cliente.enviar("POST", "/Paises/importar", token, null, ResumenImportacion.class);
    }

    public RespuestaDTO<List<Pais>> consultarExterno(String token, String nombre) {
        return cliente.enviar("GET", "/Paises/externo/" + ClienteRest.codificar(nombre.trim()), token, null, LISTA_PAISES);
    }

    public RespuestaDTO<Pais> guardar(String token, Pais pais) {
        return cliente.enviar("POST", "/Paises/guardar", token, pais, Pais.class);
    }

    private static void agregar(StringJoiner query, String clave, String valor) {
        if (valor != null && !valor.isBlank()) {
            query.add(clave + "=" + ClienteRest.codificar(valor.trim()));
        }
    }
}
