package LogicaTienda.Utils;

import java.util.logging.Logger;

/**
 * DEPRECATED: Use H2Initializer instead.
 * Esta clase se mantiene solo para compatibilidad hacia atrás.
 * Todos los métodos son delegados a H2Initializer.
 */
@Deprecated(since = "2.0", forRemoval = true)
public class MongoDBInitializer {
    private static final Logger LOGGER = Logger.getLogger(MongoDBInitializer.class.getName());

    private MongoDBInitializer() {
    }

    /**
     * Inicializa la conexión con la base de datos H2 y ejecuta la migración de datos.
     * @deprecated Use {@link H2Initializer#initialize()} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static synchronized void initialize() {
        H2Initializer.initialize();
    }

    /**
     * Detiene la aplicación y cierra las conexiones.
     * @deprecated Use {@link H2Initializer#shutdown()} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static synchronized void shutdown() {
        H2Initializer.shutdown();
    }
}
