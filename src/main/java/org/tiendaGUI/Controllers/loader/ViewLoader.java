package org.tiendaGUI.Controllers.loader;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewLoader {

    private static final Logger LOGGER = Logger.getLogger(ViewLoader.class.getName());

    public static void cargarVista(ActionEvent event, String fxmlFile, String title) {
        try {
            URL url = ViewLoader.class.getResource("/org/tiendaGUI/" + fxmlFile);
            if (url == null) {
                throw new RuntimeException("No se encontró el archivo FXML: " + fxmlFile);
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
            LOGGER.info("✅ Ventana cargada: " + title);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al cargar la vista: " + fxmlFile, e);
            mostrarAlerta("Error", "No se pudo cargar la vista: " + fxmlFile, Alert.AlertType.ERROR);
        }
    }

    public static void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
