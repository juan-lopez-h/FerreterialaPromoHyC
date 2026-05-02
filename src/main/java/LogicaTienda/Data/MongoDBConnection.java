package LogicaTienda.Data;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DEPRECATED: Use H2Connection instead.
 * Esta clase se mantiene solo para compatibilidad hacia atrás.
 * Todos los métodos son delegados a H2Connection.
 */
@Deprecated(since = "2.0", forRemoval = true)
public class MongoDBConnection {
    private MongoDBConnection() {
    }

    /**
     * @deprecated Use {@link H2Connection#getConnection()} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static Connection getConnection() throws SQLException {
        return H2Connection.getConnection();
    }

    /**
     * @deprecated Use {@link H2Connection#initialize()} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static void initialize() {
        H2Connection.initialize();
    }

    /**
     * @deprecated Use {@link H2Connection#closeConnection()} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static void closeConnection() {
        H2Connection.closeConnection();
    }

    /**
     * @deprecated Use {@link H2Connection#getDatabaseFilePath()} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static String getDatabaseFilePath() {
        return H2Connection.getDatabaseFilePath();
    }
}
