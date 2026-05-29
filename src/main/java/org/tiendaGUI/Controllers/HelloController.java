package org.tiendaGUI.Controllers;

import LogicaTienda.Model.Productos;
import LogicaTienda.Services.ProductoService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloController {
    private static final Logger LOGGER = Logger.getLogger(HelloController.class.getName());
    
    @FXML private Button botonEstadisticas, botonInventario, botonVentas;
    @FXML private Pane imagenDelMedio, panelPrincipal;
    @FXML private ToolBar panelAbajo;
    @FXML private Label panelArriba;
    @FXML private javafx.scene.image.ImageView logoImageView;

    /**
     * Inicializa el controlador principal del sistema.
     * Carga productos desde MongoDB.
     */
    @FXML
    public void initialize() {
        LOGGER.info("📦 HelloController inicializado");
        cargarProductosDesdeMongoDB();
        cargarImagen();
    }
    
    private void cargarImagen() {
        try {
            // Cargar la imagen desde los recursos
            String imagePath = "/org/tiendaGUI/images/LogoFerreteria.png";
            LOGGER.info("🔍 Intentando cargar imagen desde ruta: " + imagePath);
            
            // Obtener la URL del recurso para propósitos de depuración
            java.net.URL imageUrl = getClass().getResource(imagePath);
            LOGGER.info("📄 URL de la imagen: " + (imageUrl != null ? imageUrl.toString() : "No encontrada"));
            
            // Intentar cargar la imagen
            try (java.io.InputStream is = getClass().getResourceAsStream(imagePath)) {
                if (is != null) {
                    LOGGER.info("📦 Flujo de entrada de imagen obtenido correctamente");
                    javafx.scene.image.Image image = new javafx.scene.image.Image(is);
                    
                    // Verificar si la imagen se cargó correctamente
                    if (image.isError()) {
                        LOGGER.severe("❌ Error al cargar la imagen: " + image.getException().getMessage());
                    } else {
                        LOGGER.info(String.format("✅ Imagen cargada correctamente. Dimensiones: %.2fx%.2f", 
                            image.getWidth(), image.getHeight()));
                        logoImageView.setImage(image);
                    }
                } else {
                    LOGGER.severe("❌ No se pudo obtener el flujo de entrada para la imagen: " + imagePath);
                    
                    // Listar los recursos en el directorio /images para depuración
                    try {
                        java.net.URL imagesDir = getClass().getResource("/images");
                        if (imagesDir != null) {
                            java.nio.file.Path imagesPath = java.nio.file.Paths.get(imagesDir.toURI());
                            LOGGER.info("📂 Contenido del directorio de imágenes:");
                            java.nio.file.Files.list(imagesPath).forEach(file -> 
                                LOGGER.info("   - " + file.getFileName())
                            );
                        } else {
                            LOGGER.warning("⚠️ No se pudo encontrar el directorio /images en el classpath");
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "⚠️ No se pudo listar el directorio de imágenes", e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error inesperado al cargar la imagen", e);
        }
    }

    private void cargarProductosDesdeMongoDB() {
        Thread thread = new Thread(() -> {
            try {
                LOGGER.info("📡 Cargando productos desde MongoDB...");
                List<Productos> productos = ProductoService.obtenerTodosLosProductos();
                
                Platform.runLater(() -> {
                    if (productos != null && !productos.isEmpty()) {
                        LOGGER.info("✅ " + productos.size() + " productos cargados desde MongoDB");
                    } else {
                        LOGGER.info("ℹ️ No se encontraron productos en la base de datos");
                        mostrarAlerta("Información", "No hay productos disponibles en la base de datos.", AlertType.INFORMATION);
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "❌ Error al cargar productos desde MongoDB", e);
                Platform.runLater(() -> 
                    mostrarAlerta("Error de conexión", 
                                "No se pudieron cargar los productos desde la base de datos.\nError: " + e.getMessage(), 
                                AlertType.ERROR)
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void presionarBotonInventario(ActionEvent event) {
        cambiarVentana(event, "inventario-view.fxml", "Inventario");
    }

    @FXML
    private void presionarBotonEstadisticas(ActionEvent event) {
        cambiarVentana(event, "estadisticas-simple-view.fxml", "Estadísticas");
    }

    @FXML
    private void presionarBotonVentas(ActionEvent event) {
        cambiarVentana(event, "ventas-view.fxml", "Ventas");
    }

    @FXML
    private void presionarBotonEditorFacturas(ActionEvent event) {
        cambiarVentana(event, "editor-facturas-view.fxml", "Editor de Facturas");
    }

    /**
     * Cambia de vista a otra interfaz FXML.
     */
    private void cambiarVentana(ActionEvent event, String fxmlFile, String titulo) {
        try {
            // Normalizar el path del archivo
            String normalizedPath = fxmlFile.startsWith("/") ? fxmlFile : "/" + fxmlFile;
            URL url = getClass().getResource(normalizedPath);
            
            if (url == null) {
                // Intentar con la ruta completa si no se encuentra
                String fullPath = "/org/tiendaGUI/" + fxmlFile.replaceAll("^/+", "");
                url = getClass().getResource(fullPath);
                
                if (url == null) {
                    throw new IOException("No se pudo encontrar el archivo en ninguna de las ubicaciones: " + 
                                       normalizedPath + " o " + fullPath);
                }
            }
            
            LOGGER.info("🔄 Cargando vista desde: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.show();
            LOGGER.info("✅ Vista cargada: " + titulo);
            
        } catch (IOException e) {
            String errorMsg = "Error al cargar la vista: " + fxmlFile + " - " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            mostrarAlerta("Error", errorMsg, AlertType.ERROR);
        } catch (Exception e) {
            String errorMsg = "Error inesperado al cambiar de ventana: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            mostrarAlerta("Error", errorMsg, AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        try {
            Platform.runLater(() -> {
                try {
                    Alert alerta = new Alert(tipo);
                    alerta.setTitle(titulo);
                    alerta.setHeaderText(null);
                    alerta.setContentText(mensaje);
                    alerta.showAndWait();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al mostrar alerta", e);
                    // Si falla la alerta, al menos lo registramos
                    System.err.println(titulo + ": " + mensaje);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al mostrar alerta", e);
            System.err.println("[ERROR] " + titulo + ": " + mensaje);
        }
    }
}
