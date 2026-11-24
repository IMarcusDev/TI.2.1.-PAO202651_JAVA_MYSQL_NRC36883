package ec.edu.monster.servicios;

import ec.edu.monster.modelos.Reporte;
import ec.edu.monster.modelos.RespuestaDTO;

/**
 * Reportes estadísticos: GET /Reportes.
 *
 * @author Escobar Marcos
 */
public class ReporteServicio {

    private final ClienteRest cliente;

    public ReporteServicio(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public RespuestaDTO<Reporte> generar(String token) {
        return cliente.enviar("GET", "/Reportes", token, null, Reporte.class);
    }
}
