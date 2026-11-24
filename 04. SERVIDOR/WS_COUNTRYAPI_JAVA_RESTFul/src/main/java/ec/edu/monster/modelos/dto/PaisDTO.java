package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.data.Region;
import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * País tal como viaja entre el servidor y los clientes: la región va como texto y
 * «guardado» indica, en la consulta a la API, si el país ya está en la base de datos.
 *
 * @author Escobar Marcos
 */
@JsonbPropertyOrder({"id", "nombre", "capital", "region", "subregion", "poblacion", "area", "moneda", "guardado"})
public class PaisDTO {

    private int id;
    private String nombre;
    private String capital;
    private String region;
    private String subregion;
    private long poblacion;
    private double area;
    private String moneda;
    private boolean guardado;

    public PaisDTO() {
    }

    /** Copia los datos de un país; guardado indica si ese país ya existe en la base de datos. */
    public static PaisDTO desde(Pais pais, boolean guardado) {
        PaisDTO dto = new PaisDTO();
        dto.id = pais.getId();
        dto.nombre = pais.getNombre();
        dto.capital = pais.getCapital();
        dto.region = pais.getNombreRegion();
        dto.subregion = pais.getSubregion();
        dto.poblacion = pais.getPoblacion();
        dto.area = pais.getArea();
        dto.moneda = pais.getMoneda();
        dto.guardado = guardado;
        return dto;
    }

    /**
     * Convierte el DTO recibido del cliente en un país. La región queda solo con su nombre;
     * el controlador la busca en el catálogo y completa su id.
     */
    public Pais aPais() {
        Pais pais = new Pais(nombre, capital, region == null ? null : new Region(0, region),
                subregion, poblacion, area, moneda);
        pais.setId(id);
        return pais;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSubregion() {
        return subregion;
    }

    public void setSubregion(String subregion) {
        this.subregion = subregion;
    }

    public long getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(long poblacion) {
        this.poblacion = poblacion;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public boolean isGuardado() {
        return guardado;
    }

    public void setGuardado(boolean guardado) {
        this.guardado = guardado;
    }
}
