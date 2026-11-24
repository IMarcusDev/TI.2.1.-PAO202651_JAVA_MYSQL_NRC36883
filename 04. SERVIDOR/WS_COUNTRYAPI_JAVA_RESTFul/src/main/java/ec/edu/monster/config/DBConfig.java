package ec.edu.monster.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entrega conexiones JDBC a la base api_paises de MySQL.
 *
 * <p>La URL, el usuario y la clave no están escritos en el código: se leen del archivo
 * db.properties del classpath (src/main/resources). Si el archivo o una clave faltan,
 * se usan los valores por defecto: jdbc:mysql://localhost:3306/api_paises, usuario root
 * y clave vacía.</p>
 *
 * @author Escobar Marcos
 */
public final class DBConfig {

    private static final Logger LOG = Logger.getLogger(DBConfig.class.getName());

    private static final String URL;
    private static final String USUARIO;
    private static final String CLAVE;

    static {
        Properties propiedades = new Properties();
        try (InputStream entrada = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (entrada != null) {
                propiedades.load(entrada);
            } else {
                LOG.warning("No se encontró db.properties; se usan los valores por defecto");
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo leer db.properties; se usan los valores por defecto", e);
        }
        URL = propiedades.getProperty("db.url", "jdbc:mysql://localhost:3306/api_paises");
        USUARIO = propiedades.getProperty("db.usuario", "root");
        CLAVE = propiedades.getProperty("db.clave", "");

        // En Payara el controlador del war no siempre se registra solo en DriverManager,
        // por eso se carga de forma explícita.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "No se encontró el controlador JDBC de MySQL", e);
        }
    }

    private DBConfig() {
    }

    /** Abre una conexión nueva; quien la pide debe cerrarla (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
