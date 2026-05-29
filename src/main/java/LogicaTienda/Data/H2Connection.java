package LogicaTienda.Data;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Clase de acceso a la base de datos H2 embebida.
 * Proporciona métodos estáticos para obtener conexiones y gestionar la inicialización.
 */
public class H2Connection {
    private H2Connection() {
    }

    /**
     * Obtiene una conexión a la base de datos H2.
     *
     * @return conexión SQL
     * @throws SQLException si hay error al conectar
     */
    public static Connection getConnection() throws SQLException {
        return H2Database.getConnection();
    }

    /**
     * Inicializa la base de datos H2 y crea las tablas necesarias.
     */
    public static void initialize() {
        H2Database.initialize();
    }

    /**
     * Cierra la conexión a la base de datos (no implementado para H2 en memoria/archivo).
     */
    public static void closeConnection() {
        // La conexión H2 se abre y cierra por operación.
    }

    /**
     * Obtiene la ruta del archivo de la base de datos H2.
     *
     * @return ruta absoluta del archivo ferreteria.h2.db
     */
    public static String getDatabaseFilePath() {
        return H2Database.getDatabaseFilePath();
    }
}

