package LogicaTienda.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class H2Database {
    private static final Logger LOGGER = Logger.getLogger(H2Database.class.getName());
    private static final Path DB_DIRECTORY = Paths.get(System.getProperty("user.home"), ".ferreteria-la-promo-hc");
    private static final String DB_NAME = "ferreteria";
    private static final String JDBC_URL = buildJdbcUrl();
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static volatile boolean initialized = false;

    private H2Database() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            Files.createDirectories(DB_DIRECTORY);
            // Abrimos la conexión directamente con DriverManager para evitar la recursión
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS productos (" +
                        "id_producto VARCHAR(64) PRIMARY KEY, " +
                        "nombre VARCHAR(255) NOT NULL, " +
                        "precio DOUBLE PRECISION NOT NULL, " +
                        "precio_para_vender DOUBLE PRECISION NOT NULL, " +
                        "porcentaje_ganancia DOUBLE PRECISION NOT NULL, " +
                        "cantidad INT NOT NULL, " +
                        "stock INT NOT NULL"
                        + ")");

                statement.execute("CREATE TABLE IF NOT EXISTS facturas (" +
                        "id VARCHAR(64) PRIMARY KEY, " +
                        "fecha TIMESTAMP NOT NULL, " +
                        "cliente_nombre VARCHAR(255), " +
                        "cliente_identificacion VARCHAR(100), " +
                        "tipo_documento VARCHAR(50), " +
                        "cliente_email VARCHAR(255), " +
                        "cliente_telefono VARCHAR(100), " +
                        "tipo_factura VARCHAR(50), " +
                        "estado VARCHAR(50), " +
                        "eliminada BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "total DOUBLE PRECISION NOT NULL DEFAULT 0, " +
                        "metodo_pago VARCHAR(100), " +
                        "referencia_pago VARCHAR(500), " +
                        "pago_json CLOB"
                        + ")");

                statement.execute("CREATE TABLE IF NOT EXISTS factura_detalle (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "factura_id VARCHAR(64) NOT NULL, " +
                        "producto_id VARCHAR(64) NOT NULL, " +
                        "nombre_producto VARCHAR(255), " +
                        "cantidad INT NOT NULL, " +
                        "precio_unitario DOUBLE PRECISION NOT NULL, " +
                        "subtotal DOUBLE PRECISION NOT NULL, " +
                        "CONSTRAINT fk_factura_detalle_factura FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE CASCADE"
                        + ")");

                statement.execute("CREATE TABLE IF NOT EXISTS domicilios (" +
                        "id VARCHAR(64) PRIMARY KEY, " +
                        "direccion VARCHAR(255), " +
                        "referencia_direccion VARCHAR(255), " +
                        "numero_postal VARCHAR(50), " +
                        "numero_apartamento VARCHAR(50), " +
                        "numero_celular VARCHAR(50), " +
                        "fecha_entrega DATE, " +
                        "id_factura VARCHAR(64), " +
                        "cliente_identificacion VARCHAR(100), " +
                        "estado_domicilio VARCHAR(50)"
                        + ")");

                statement.execute("CREATE TABLE IF NOT EXISTS app_meta (" +
                        "meta_key VARCHAR(100) PRIMARY KEY, " +
                        "meta_value VARCHAR(500) NOT NULL"
                        + ")");
            }

            initialized = true;
            LOGGER.info("✅ Base de datos H2 inicializada en: " + getDatabaseFilePath());
        } catch (IOException | SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Error al inicializar la base de datos H2", e);
            throw new RuntimeException("No se pudo inicializar la base de datos local", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        initialize();

        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static String getDatabaseFilePath() {
        return DB_DIRECTORY.resolve(DB_NAME).toAbsolutePath().toString();
    }

    private static String buildJdbcUrl() {
        String path = DB_DIRECTORY.resolve(DB_NAME).toAbsolutePath().toString().replace("\\", "/");
        return "jdbc:h2:file:" + path + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";
    }
}
