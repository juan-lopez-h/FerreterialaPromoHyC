package org.tiendaGUI.Controllers;

import LogicaTienda.Model.Productos;
import LogicaTienda.Services.ProductoService;
import LogicaTienda.Forms.formularioProduct;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InventarioController implements Initializable {
    @FXML private Button btnNuevo, btnVolver, btnEliminar, btnActualizar;
    @FXML private TextField txtBusqueda;
    @FXML private TableView<Productos> tablaNumero1;
    @FXML private TableColumn<Productos, String> columnaNombre;
    @FXML private TableColumn<Productos, Double> columnaPrecio;
    @FXML private TableColumn<Productos, Double> columnaPrecioVenta;
    @FXML private TableColumn<Productos, Integer> columnaCantidad;
    @FXML private TableColumn<Productos, String> columnaId;

    @FXML
    private void volver(ActionEvent event) {
        cambiarVentana(event, "hello-view.fxml", "Menú Principal");
    }

    @FXML
    private void presionarBotonNuevo(ActionEvent event) {
        new formularioProduct("Nuevo Producto", 
            FXCollections.observableArrayList(ProductoService.obtenerTodosLosProductos()), 
            false, null, null)
                .showAndWait();
        actualizarTabla();
    }

    @FXML
    private void presionarBotonEliminar(ActionEvent event) {
        Productos productoSeleccionado = tablaNumero1.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            mostrarAlerta("Error", "No se seleccionó ningún producto para eliminar.", Alert.AlertType.ERROR);
            return;
        }
        
        // Mostrar confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar el producto?");
        alert.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Eliminar de MongoDB
                ProductoService.eliminarProducto(productoSeleccionado.getIdProducto());
                actualizarTabla();
                mostrarAlerta("Éxito", "Producto eliminado correctamente.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                Logger.getLogger(InventarioController.class.getName()).log(Level.SEVERE, "Error al eliminar el producto", e);
                mostrarAlerta("Error", "No se pudo eliminar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void presionarBotonActualizar(ActionEvent event) {
        Productos productoSeleccionado = tablaNumero1.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            mostrarAlerta("Error", "No se seleccionó ningún producto para actualizar.", Alert.AlertType.ERROR);
            return;
        }
        // Abre el formulario para actualizar el producto seleccionado
        new formularioProduct("Actualizar Producto", 
            FXCollections.observableArrayList(ProductoService.obtenerTodosLosProductos()), 
            false, null, productoSeleccionado)
                .showAndWait();
        actualizarTabla();
    }

    /**
     * Actualiza la tabla desde MongoDB
     */
    private void actualizarTabla() {
        try {
            List<Productos> productos = ProductoService.obtenerTodosLosProductos();
            ObservableList<Productos> productosList = FXCollections.observableArrayList(productos);
            tablaNumero1.setItems(productosList);
            tablaNumero1.refresh();
        } catch (Exception e) {
            Logger.getLogger(InventarioController.class.getName()).log(Level.SEVERE, "Error al cargar los productos", e);
            mostrarAlerta("Error", "No se pudieron cargar los productos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        columnaPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioParaVender"));
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));

        // Cargar productos iniciales
        actualizarTabla();
    }

    protected void cambiarVentana(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/" + fxmlFile));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista: " + fxmlFile + "\nDetalles: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarAlerta(titulo, mensaje, Alert.AlertType.ERROR);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Este método se mantiene para compatibilidad pero ya no es necesario con MongoDB
    public void bindListaProductos(ObservableList<Productos> productos) {
        tablaNumero1.setItems(productos);
    }


    @FXML
    private void buscarProducto(KeyEvent event) {
        // Este método se llama cuando se suelta una tecla en el campo de búsqueda
        if (event != null) {
            ejecutarBusqueda();
        }
    }
    
    private void ejecutarBusqueda() {
        String textoBusqueda = txtBusqueda.getText().trim().toLowerCase();
        if (textoBusqueda.isEmpty()) {
            actualizarTabla();
            return;
        }

        try {
            List<Productos> todosProductos = ProductoService.obtenerTodosLosProductos();
            ObservableList<Productos> productosFiltrados = FXCollections.observableArrayList();
            
            for (Productos producto : todosProductos) {
                if ((producto.getNombre() != null && producto.getNombre().toLowerCase().contains(textoBusqueda)) ||
                    (producto.getIdProducto() != null && producto.getIdProducto().toLowerCase().contains(textoBusqueda))) {
                    productosFiltrados.add(producto);
                }
            }
            
            tablaNumero1.setItems(productosFiltrados);
            tablaNumero1.refresh();
            
        } catch (Exception e) {
            Logger.getLogger(InventarioController.class.getName()).log(Level.SEVERE, "Error al buscar productos", e);
            mostrarAlerta("Error", "No se pudieron buscar los productos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    @Deprecated
    private void MetodoBusquedaTablaCarrito() {
        // Método obsoleto, mantenido para compatibilidad
        ejecutarBusqueda();
    }

    @FXML
    private void restablecerTabla() {
        if (txtBusqueda != null) {
            txtBusqueda.clear();
        }
        actualizarTabla();
    }
    
    private boolean confirmarAccion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

}
