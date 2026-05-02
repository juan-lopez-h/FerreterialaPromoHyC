package LogicaTienda.Utils;

import LogicaTienda.Data.H2Database;
import LogicaTienda.Services.MigracionService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicializador de la base de datos H2 embebida.
 * Gestiona la creación de tablas, migración de datos y ciclo de vida de la aplicación.
 */
public class H2Initializer {
    private static final Logger LOGGER = Logger.getLogger(H2Initializer.class.getName());
    private static boolean initialized = false;

    private H2Initializer() {
    }

    /**
     * Inicializa la conexión con la base de datos H2 y ejecuta la migración de datos.
     * Este método es thread-safe y se ejecuta una sola vez.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            H2Database.initialize();
            MigracionService.migrarDatos();
            initialized = true;
            LOGGER.info("✅ Base de datos H2 inicializada correctamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al inicializar la base de datos H2", e);
            throw new RuntimeException("No se pudo inicializar la base de datos local", e);
        }
    }

    /**
     * Detiene la aplicación y cierra las conexiones.
     * Marca la aplicación como no inicializada para permitir reinicialización futura.
     */
    public static synchronized void shutdown() {
        if (initialized) {
            initialized = false;
            LOGGER.info("✅ Base de datos H2 cerrada correctamente");
        }
    }
}

