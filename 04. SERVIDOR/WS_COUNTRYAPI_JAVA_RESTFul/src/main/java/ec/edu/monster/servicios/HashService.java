package ec.edu.monster.servicios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Calcula el hash SHA-256 de las claves. La tabla usuario guarda ese hash (64 caracteres
 * hexadecimales en minúsculas), nunca la clave en texto plano.
 *
 * @author Escobar Marcos
 */
public class HashService {

    /** SHA-256 del texto en UTF-8, como 64 caracteres hexadecimales en minúsculas. */
    public String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Todas las JVM deben incluir SHA-256; si faltara, es un error de la plataforma.
            throw new IllegalStateException("SHA-256 no está disponible en la JVM", e);
        }
    }
}
