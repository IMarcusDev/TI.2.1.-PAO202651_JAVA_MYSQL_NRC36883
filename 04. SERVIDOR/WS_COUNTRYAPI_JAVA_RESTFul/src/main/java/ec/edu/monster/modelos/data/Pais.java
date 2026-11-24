package ec.edu.monster.modelos.data;

import java.time.LocalDateTime;

/**
 * Fila de la tabla pais con los siete datos del taller: nombre, capital, región, subregión,
 * población, área (km²) y moneda. La región es un objeto del catálogo region.
 *
 * @author Escobar Marcos
 */
public class Pais {

    private int id;
    private String nombre;
    private String capital;
    private Region region;
    private String subregion;
    private long poblacion;
    private double area;
    private String moneda;
    private LocalDateTime fechaActualizacion;

    public Pais() {
    }

    public Pais(String nombre, String capital, Region region, String subregion,
            long poblacion, double area, String moneda) {
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
        this.subregion = subregion;
        this.poblacion = poblacion;
        this.area = area;
        this.moneda = moneda;
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

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
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

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    /** Nombre de la región o null si el país todavía no la tiene. */
    public String getNombreRegion() {
        return region == null ? null : region.getNombre();
    }
}
