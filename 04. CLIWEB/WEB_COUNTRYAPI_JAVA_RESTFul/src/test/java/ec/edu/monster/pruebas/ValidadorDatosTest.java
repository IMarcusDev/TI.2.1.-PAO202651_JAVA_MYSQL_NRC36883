package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.Pais;
import org.junit.jupiter.api.Test;

/**
 * Validaciones del ECUD: nombre obligatorio de hasta 100 caracteres, población entera mayor o
 * igual que cero, área mayor o igual que cero y consulta de al menos dos caracteres.
 */
class ValidadorDatosTest {

    private static Pais pais(String nombre, String region, long poblacion, double area) {
        return new Pais(nombre, "Capital", region, "Sub", poblacion, area, "Moneda");
    }

    @Test
    void credenciales() {
        assertEquals("Ingrese el usuario", ValidadorDatos.validarCredenciales("  ", "x"));
        assertEquals("Ingrese la clave", ValidadorDatos.validarCredenciales("monster", ""));
        assertNull(ValidadorDatos.validarCredenciales("monster", "monster9"));
    }

    @Test
    void consultaEnLaApiConAlMenosDosCaracteres() {
        assertEquals("Ingrese al menos dos caracteres", ValidadorDatos.validarNombreConsulta(null));
        assertEquals("Ingrese al menos dos caracteres", ValidadorDatos.validarNombreConsulta(" e "));
        assertNull(ValidadorDatos.validarNombreConsulta("ec"));
    }

    @Test
    void nombreObligatorioYDeHasta100Caracteres() {
        assertEquals("Ingrese el nombre del país", ValidadorDatos.validarPais(pais(" ", "Asia", 1, 1)));
        assertEquals("Ingrese el nombre del país", ValidadorDatos.validarPais(pais("x".repeat(101), "Asia", 1, 1)));
        assertNull(ValidadorDatos.validarPais(pais("x".repeat(100), "Asia", 1, 1)));
    }

    @Test
    void regionDeLaLista() {
        assertEquals("Seleccione una región", ValidadorDatos.validarPais(pais("Chile", null, 1, 1)));
        assertEquals("Seleccione una región", ValidadorDatos.validarPais(pais("Chile", "Narnia", 1, 1)));
    }

    @Test
    void poblacionYAreaMayoresOIgualesQueCero() {
        assertEquals("La población debe ser un número entero mayor o igual que cero",
                ValidadorDatos.validarPais(pais("Chile", "América", -1, 1)));
        assertEquals("El área debe ser un número mayor o igual que cero",
                ValidadorDatos.validarPais(pais("Chile", "América", 1, -0.1)));
        assertEquals("El área debe ser un número mayor o igual que cero",
                ValidadorDatos.validarPais(pais("Chile", "América", 1, Double.NaN)));
        assertNull(ValidadorDatos.validarPais(pais("Isla", "Antártida", 0, 0)));
    }

    @Test
    void textosOpcionalesDeHasta100Caracteres() {
        Pais p = pais("Chile", "América", 1, 1);
        p.setMoneda("m".repeat(101));
        assertEquals("La capital, la subregión y la moneda deben tener como máximo 100 caracteres",
                ValidadorDatos.validarPais(p));
    }

    @Test
    void paisDeUniversidadesObligatorio() {
        assertEquals("Ingrese el país", ValidadorDatos.validarPaisUniversidades(" "));
        assertNull(ValidadorDatos.validarPaisUniversidades("Ecuador"));
    }

    @Test
    void leePoblacionConOSinSeparadoresDeMiles() {
        assertEquals(17643060L, ValidadorDatos.leerPoblacion("17643060"));
        assertEquals(17643060L, ValidadorDatos.leerPoblacion("17.643.060"));
        assertEquals(17643060L, ValidadorDatos.leerPoblacion(" 17,643,060 "));
        assertEquals(0L, ValidadorDatos.leerPoblacion("0"));
        assertEquals(-1L, ValidadorDatos.leerPoblacion("12.5"), "no es entero");
        assertEquals(-1L, ValidadorDatos.leerPoblacion("abc"));
        assertEquals(-1L, ValidadorDatos.leerPoblacion("-5"));
        assertEquals(-1L, ValidadorDatos.leerPoblacion(""));
    }

    @Test
    void leeAreaConPuntoOComaDecimal() {
        assertEquals(276841.0, ValidadorDatos.leerArea("276841"));
        assertEquals(2.5, ValidadorDatos.leerArea("2,5"));
        assertEquals(2.5, ValidadorDatos.leerArea("2.5"));
        assertEquals(0.0, ValidadorDatos.leerArea(""));
        assertTrue(Double.isNaN(ValidadorDatos.leerArea("-3")));
        assertTrue(Double.isNaN(ValidadorDatos.leerArea("diez")));
    }
}
