package ec.edu.monster.modelos;

import java.util.List;

/**
 * País con los siete datos del taller, tal como lo envía el servidor. «guardado» indica, en la
 * consulta a la API, si el país ya está en la base de datos.
 *
 * @author Escobar Marcos
 */
public class Pais {

    /** Regiones del catálogo, en el orden en que se muestran en filtros y formularios. */
    public static final List<String> REGIONES =
            List.of("América", "Europa", "Asia", "África", "Oceanía", "Antártida");

    private int id;
    private String nombre;
    private String capital;
    private String region;
    private String subregion;
    private long poblacion;
    private double area;
    private String moneda;
    private boolean guardado;

    public Pais() {
    }

    public Pais(String nombre, String capital, String region, String subregion,
            long poblacion, double area, String moneda) {
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
        this.subregion = subregion;
        this.poblacion = poblacion;
        this.area = area;
        this.moneda = moneda;
    }

    /** Copia independiente, para editar sin tocar la fila de la tabla. */
    public Pais copia() {
        Pais otro = new Pais(nombre, capital, region, subregion, poblacion, area, moneda);
        otro.id = id;
        otro.guardado = guardado;
        return otro;
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
