package ec.edu.monster.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * El hash debe coincidir con el que guarda seed.sql para los usuarios de prueba.
 */
class HashServiceTest {

    private final HashService hashService = new HashService();

    @Test
    void hashDeMonster9CoincideConSeed() {
        assertEquals("774e993500f4027acfd72b7a7ee564b76ae43cf7c4c943ed0e0f364cca16b6ec",
                hashService.sha256("monster9"));
    }

    @Test
    void hashDeAdmin123CoincideConSeed() {
        assertEquals("240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9",
                hashService.sha256("admin123"));
    }

    @Test
    void hashTiene64CaracteresEnMinusculas() {
        String hash = hashService.sha256("Clave con tildes: áéí");
        assertEquals(64, hash.length());
        assertEquals(hash.toLowerCase(), hash);
    }
}
