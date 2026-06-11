package org.tiendaGUI.Controllers;

import LogicaTienda.Forms.SearchForm;
import LogicaTienda.Model.Productos;
import LogicaTienda.Services.ProductoService;
import org.tiendaGUI.DTO.CarritoDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PedidoController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(PedidoController.class.getName());

    @FXML
    private TableView<Productos> tblProductos;
    @FXML
    private TableColumn<Productos, String> columnaNombre;
    @FXML
    private TableColumn<Productos, Double> columnaValor;
    @FXML
    private TableColumn<Productos, Integer> columnaCantidad;
    @FXML
    private TableColumn<Productos, String> ColumnaIdProducto;
    @FXML
    private Label lblTotal;

    @FXML
    private Button btnFacturaElectro;
    @FXML
    private Button btnImprimirFactura;
    @FXML
    private Button btnAgregarProducto;
    @FXML
    private Button btnEliminarProducto;
    @FXML
    private Button btnDomicilio;
    @FXML
    private Button btnVolver;
    @FXML
    private Button btnPagar;

    // DTO recibido desde el controlador anterior
    private CarritoDTO carritoDTO;
    private ObservableList<Productos> productosEnCarrito;

    public void setCarritoDTO(CarritoDTO carritoDTO) {
        this.carritoDTO = carritoDTO;
        cargarDatosDesdeDTO();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas
        ColumnaIdProducto.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaValor.setCellValueFactory(new PropertyValueFactory<>("precioParaVender"));
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        // Formatear columna de valor
        columnaValor.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", precio));
                }
            }
        });

        // Inicializar lista observable para el carrito
        productosEnCarrito = FXCollections.observableArrayList();
        tblProductos.setItems(productosEnCarrito);

        // Configurar selección múltiple
        tblProductos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Actualizar total cuando cambie la lista de productos
        productosEnCarrito.addListener((javafx.collections.ListChangeListener.Change<? extends Productos> c) -> actualizarTotal());
    }

    private void cargarDatosDesdeDTO() {
        if (carritoDTO == null || carritoDTO.getProductos() == null) {
            mostrarAlerta("Error", "No hay datos de carrito para mostrar.", Alert.AlertType.ERROR);
            return;
        }
        // Actualizar la lista de productos en la tabla
        productosEnCarrito.setAll(carritoDTO.getProductos());
    }

    @FXML
    private void eliminarProducto(ActionEvent event) {
        Productos seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Seleccione un producto para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        // Mostrar confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Eliminar producto del carrito");
        confirmacion.setContentText(String.format("¿Está seguro de eliminar %s del carrito?",
                seleccionado.getNombre()));

        confirmacion.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {
                // Buscar el producto en la base de datos
                Productos productoEnBD = ProductoService.buscarProductoPorId(seleccionado.getIdProducto());

                if (productoEnBD != null) {
                    // Devolver la cantidad al inventario
                    int cantidadADevolver = seleccionado.getCantidad();

                    // Actualizar la cantidad en la base de datos
                    productoEnBD.setCantidad(productoEnBD.getCantidad() + cantidadADevolver);

                    try {
                        // Guardar los cambios en la base de datos
                        ProductoService.actualizarProducto(productoEnBD);

                        // Actualizar la vista
                        Platform.runLater(() -> {
                            // Eliminar del carrito
                            productosEnCarrito.remove(seleccionado);
                            if (carritoDTO != null) {
                                carritoDTO.getProductos().remove(seleccionado);
                                carritoDTO.actualizarTotal();
                            }

                            // Actualizar la interfaz
                            actualizarTotal();
                            tblProductos.refresh();

                            mostrarAlerta("Éxito", "Producto eliminado del carrito.", Alert.AlertType.INFORMATION);
                        });
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error al actualizar el inventario", e);
                        mostrarAlerta("Error", "No se pudo actualizar el inventario: " + e.getMessage(),
                                Alert.AlertType.ERROR);
                    }
                } else {
                    // Si el producto no está en la BD, solo lo quitamos del carrito
                    Platform.runLater(() -> {
                        productosEnCarrito.remove(seleccionado);
                        if (carritoDTO != null) {
                            carritoDTO.getProductos().remove(seleccionado);
                            carritoDTO.actualizarTotal();
                        }
                        actualizarTotal();
                        tblProductos.refresh();

                        mostrarAlerta("Advertencia",
                                "El producto no se encontró en el inventario, pero fue eliminado del carrito.",
                                Alert.AlertType.WARNING);
                    });
                }
            }
        });
    }

    @FXML
    private void BtnAgregarProductoOnAction(ActionEvent event) {
        cambiarVentanaConDTO(event, "Ventas-view.fxml", "Ventas");
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        // Devolver productos al inventario antes de volver
        if (carritoDTO != null && carritoDTO.getProductos() != null && !carritoDTO.getProductos().isEmpty()) {
            // Devolver productos al inventario automáticamente
            for (Productos producto : carritoDTO.getProductos()) {
                try {
                    ProductoService.actualizarStock(
                        producto.getIdProducto(),
                        producto.getCantidad(),
                        false // false para devolver al stock (añadir)
                    );
                    LOGGER.log(Level.INFO, "Producto devuelto al inventario: " + producto.getIdProducto() + ", Cantidad: " + producto.getCantidad());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al devolver producto al inventario: " + producto.getIdProducto(), e);
                }
            }
            // Limpiar carrito
            carritoDTO.getProductos().clear();
            carritoDTO.actualizarTotal();

            mostrarAlerta("Operación completada", "Los productos han sido devueltos al inventario.", Alert.AlertType.INFORMATION);
        }

        cambiarVentanaConDTO(event, "Ventas-view.fxml", "Ventas");
    }

    @FXML
    private void irADomicilio(ActionEvent event) {
        if (tblProductos.getItems().isEmpty()) {
            mostrarAlerta("Carrito vacío", "Agrega productos antes de continuar.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/domicilio-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Domicilio");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista de Domicilio.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void MetodoBusquedaTablaCarrito(ActionEvent event) {
        // Abrir el formulario de búsqueda con los productos actuales del carritoDTO
        SearchForm searchForm = new SearchForm(carritoDTO.getProductos());

        // Cuando el formulario se cierre, actualizar la tabla con los productos filtrados
        searchForm.setOnBusquedaFinalizada(productosFiltrados -> {
            // SOLO actualizar la tabla con la lista filtrada sin tocar el DTO
            tblProductos.setItems(FXCollections.observableArrayList(productosFiltrados));
        });

        searchForm.showAndWait();
    }

    @FXML
    private void irApagar(ActionEvent event) {
        if (tblProductos.getItems().isEmpty() || calcularTotalCarrito() == 0) {
            mostrarAlerta("Carrito vacío", "Agrega productos antes de continuar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Cargar directamente la pantalla de pago
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/pago-view.fxml"));
            Parent root = loader.load();

            // Configurar el controlador de pago con el carrito
            PagoController pagoController = loader.getController();
            pagoController.setCarritoDTO(carritoDTO);

            // Mostrar la pantalla de pago
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Procesar Pago");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar la pantalla de pago", e);
            mostrarAlerta("Error", "No se pudo cargar la pantalla de pago: " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }

    private double calcularTotalCarrito() {
        if (tblProductos.getItems() == null || tblProductos.getItems().isEmpty()) {
            return 0.0;
        }
        return tblProductos.getItems().stream()
                .filter(Objects::nonNull)
                .mapToDouble(p -> p.getPrecioParaVender() * p.getCantidad())
                .sum();
    }

    private void actualizarTotal() {
        double total = calcularTotalCarrito();
        if (carritoDTO != null) {
            carritoDTO.setTotal(total);
        }
        if (lblTotal != null) {
            Platform.runLater(() -> {
                lblTotal.setText(String.format("Total: $%,.2f", total));
            });
        }
    }

    @FXML
    private void generarFacturaElectronica(ActionEvent event) {
        mostrarAlerta("Factura", "Factura electrónica generada (pendiente).", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void imprimirFactura(ActionEvent event) {
        irApagar(event);
    }

    @FXML
    private void RestablecerTablaAction(ActionEvent event) {
        tblProductos.setItems(FXCollections.observableArrayList(carritoDTO.getProductos()));
    }

    private void cambiarVentanaConDTO(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar la ventana: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(tipo);
            alerta.setTitle(titulo);
            alerta.setHeaderText(null);
            alerta.setContentText(mensaje);
            alerta.showAndWait();
        });
    }
}
