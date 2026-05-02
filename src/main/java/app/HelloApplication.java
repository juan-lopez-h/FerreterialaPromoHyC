package app;

import LogicaTienda.Utils.H2Initializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal de la aplicación Ferretería La Promo H&C.
 */
public class HelloApplication extends Application {
    private static final String TITULO_APLICACION = "Ferretería La Promo H&C";
    private static final int ANCHO_VENTANA = 1024;
    private static final int ALTO_VENTANA = 768;
    private static final Logger LOGGER = Logger.getLogger(HelloApplication.class.getName());

    /**
     * Método principal de inicio de la aplicación JavaFX.
     * 
     * @param stage El escenario principal de la aplicación
     */
    @Override
    public void start(Stage stage) {
        try {
            H2Initializer.initialize();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), ANCHO_VENTANA, ALTO_VENTANA);

            stage.setTitle(TITULO_APLICACION);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            LOGGER.info("✅ Aplicación iniciada correctamente con base local H2");
        } catch (Exception e) {
            manejarErrorInicio(e);
        }
    }

    /**
     * Maneja los errores que ocurren durante el inicio de la aplicación.
     * 
     * @param e La excepción que se produjo
     */
    private void manejarErrorInicio(Exception e) {
        LOGGER.log(Level.SEVERE, "❌ Error al iniciar la aplicación", e);
        showErrorDialog("No se pudo iniciar la aplicación: " + e.getMessage());
        System.exit(1);
    }

    /**
     * Muestra un diálogo de error al usuario.
     * 
     * @param message El mensaje de error a mostrar
     */
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al iniciar");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Punto de entrada principal de la aplicación.
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        try {
            launch();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fatal en la aplicación", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        try {
            LOGGER.info("🔴 Cerrando la aplicación...");
            H2Initializer.shutdown();
            LOGGER.info("✅ Aplicación cerrada correctamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al cerrar la aplicación", e);
        }
    }
}
