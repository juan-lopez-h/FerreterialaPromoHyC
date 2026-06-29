package org.tiendaGUI.Controllers;

import LogicaTienda.Model.Productos;
import LogicaTienda.Services.ProductoService;
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
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.tiendaGUI.Controllers.PedidoController;
import org.tiendaGUI.Controllers.loader.ViewLoader;
import org.tiendaGUI.DTO.CarritoDTO;
import org.tiendaGUI.DTO.ProductoSimpleDTO;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VentasController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(VentasController.class.getName());

    @FXML private Button btnVolver;
    @FXML private Button btnVender;
    @FXML private Button btnActualizar;
    @FXML private Button btnIrCarrito;

    @FXML private TableView<ProductoSimpleDTO> tablaProductos;
    @FXML private TableColumn<ProductoSimpleDTO, String> columnaNombre;
    @FXML private TableColumn<ProductoSimpleDTO, Double> columnaPrecio;
    @FXML private TableColumn<ProductoSimpleDTO, Integer> columnaCantidad;
    @FXML private TableColumn<ProductoSimpleDTO, Integer> columnaStock;
    @FXML private TableColumn<ProductoSimpleDTO, String> columnaId;
    @FXML private TextField txtBusqueda;

    private ObservableList<ProductoSimpleDTO> productosDTO;

    private CarritoDTO carritoDTO;
    private boolean volverAlCarrito = false;

    public void setCarritoDTO(CarritoDTO carritoDTO) {
        this.carritoDTO = carritoDTO;
        if (carritoDTO != null && carritoDTO.getProductos() != null) {
            volverAlCarrito = true;
            // Actualizar la tabla con los productos del carrito si existe
            cargarProductos();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(" Inicializando controlador de ventas con MongoDB");

        // Configurar columnas de la tabla
        columnaId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Inicializar lista observable
        productosDTO = FXCollections.observableArrayList();
        tablaProductos.setItems(productosDTO);
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cargar productos desde MongoDB
        cargarProductosDesdeMongoDB();

        txtBusqueda.setOnKeyPressed(this::buscarProducto);
    }

    private void cargarProductos() {
        Thread thread = new Thread(() -> {
            try {
                List<Productos> productos = ProductoService.obtenerTodosLosProductos();
                Platform.runLater(() -> {
                    try {
                        if (productos != null && !productos.isEmpty()) {
                            // Convertir a DTOs y actualizar la tabla
                            List<ProductoSimpleDTO> listaProductos = new ArrayList<>();
                            for (Productos p : productos) {
                                ProductoSimpleDTO dto = new ProductoSimpleDTO(
                                    p.getIdProducto(),
                                    p.getNombre(),
                                    p.getPrecioParaVender(),
                                    0, // Cantidad inicial
                                    p.getCantidad()
                                );
                                listaProductos.add(dto);
                            }

                            productosDTO.setAll(listaProductos);
                            tablaProductos.refresh();
                            System.out.println(productos.size() + " productos cargados");
                        } else {
                            System.out.println("No se encontraron productos");
                        }
                    } catch (Exception e) {
                        mostrarAlerta("Error", "Error al procesar los productos: " + e.getMessage(), 
                                    Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> 
                    mostrarAlerta("Error", "No se pudieron cargar los productos: " + e.getMessage(), 
                                Alert.AlertType.ERROR)
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void cargarProductosDesdeMongoDB() {
        cargarProductos();
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



    @FXML
    private void btnVolverAction(ActionEvent event) {
        if (volverAlCarrito && carritoDTO != null) {
            volverAlCarrito(event);
            return;
        }

        System.out.println(" Volviendo al men principal...");

        // Devolver productos al inventario si hay productos en el carrito
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

        cambiarVentana(event, "hello-view.fxml", "Men Principal");
    }

    private void volverAlCarrito(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/pedido-view.fxml"));
            Parent root = loader.load();
            PedidoController pedidoController = loader.getController();
            pedidoController.setCarritoDTO(carritoDTO);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Carrito");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver al carrito", e);
            mostrarAlerta("Error", "No se pudo regresar al carrito: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void btnActualizarAction(ActionEvent event) {
        cargarProductos();
    }

    @FXML
    private void buscarProducto(KeyEvent event) {
        if (event != null) {
            ejecutarBusqueda();
        }
    }

    @FXML
    private void restablecerBusqueda() {
        if (txtBusqueda != null) {
            txtBusqueda.clear();
        }
        cargarProductos();
    }

    private void ejecutarBusqueda() {
        String textoBusqueda = txtBusqueda.getText().trim().toLowerCase();
        if (textoBusqueda.isEmpty()) {
            cargarProductos();
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                List<Productos> todosProductos = ProductoService.obtenerTodosLosProductos();
                List<ProductoSimpleDTO> productosFiltrados = new ArrayList<>();

                for (Productos producto : todosProductos) {
                    if ((producto.getNombre() != null && producto.getNombre().toLowerCase().contains(textoBusqueda)) ||
                        (producto.getIdProducto() != null && producto.getIdProducto().toLowerCase().contains(textoBusqueda))) {
                        ProductoSimpleDTO dto = new ProductoSimpleDTO(
                            producto.getIdProducto(),
                            producto.getNombre(),
                            producto.getPrecioParaVender(),
                            0, // Cantidad inicial
                            producto.getCantidad()
                        );
                        productosFiltrados.add(dto);
                    }
                }

                Platform.runLater(() -> {
                    try {
                        productosDTO.setAll(productosFiltrados);
                        tablaProductos.refresh();
                    } catch (Exception e) {
                        mostrarAlerta("Error", "Error al actualizar la tabla: " + e.getMessage(), 
                                    Alert.AlertType.ERROR);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> 
                    mostrarAlerta("Error", "No se pudieron buscar los productos: " + e.getMessage(), 
                                Alert.AlertType.ERROR)
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void btnVenderAction(ActionEvent event) {
        ProductoSimpleDTO seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Selecciona un producto para vender.", Alert.AlertType.ERROR);
            return;
        }

        // Obtener el producto directamente de MongoDB
        Productos producto = ProductoService.buscarProductoPorId(seleccionado.getIdProducto());

        if (producto == null) {
            mostrarAlerta("Error", "El producto seleccionado no existe o no está disponible.", Alert.AlertType.ERROR);
            return;
        }
        int stockDisponible = producto.getCantidad() + producto.getStock();

        // Diálogo para ingresar la cantidad
        TextInputDialog dialogoCantidad = new TextInputDialog("1");
        dialogoCantidad.setTitle("Cantidad a Vender");
        dialogoCantidad.setHeaderText(String.format("Venta de producto: %s\nStock disponible: %d", 
            producto.getNombre(), stockDisponible));
        dialogoCantidad.setContentText("Ingrese la cantidad a vender:");

        dialogoCantidad.showAndWait().ifPresent(input -> {
            try {
                int cantidadDeseada = Integer.parseInt(input);
                if (cantidadDeseada <= 0) {
                    throw new NumberFormatException("La cantidad debe ser mayor a cero");
                }

                if (cantidadDeseada > stockDisponible) {
                    mostrarAlerta("Error", "No hay suficiente stock disponible.", Alert.AlertType.ERROR);
                    return;
                }

                // Calcular cuánto vender de cantidad y cuánto de stock
                int restante = cantidadDeseada;
                if (producto.getCantidad() >= restante) {
                    producto.setCantidad(producto.getCantidad() - restante);
                } else {
                    restante -= producto.getCantidad();
                    producto.setCantidad(0);
                    producto.setStock(producto.getStock() - restante);
                }

                // Actualizar el producto en MongoDB
                try {
                    ProductoService.actualizarProducto(producto);
                    // Si llegamos aquí, la actualización fue exitosa
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al actualizar el producto en MongoDB", e);
                    throw new Exception("No se pudo actualizar el inventario: " + e.getMessage());
                }

                // Crear una copia del producto para el carrito
                Productos productoCarrito = new Productos(
                    producto.getIdProducto(), 
                    producto.getNombre(), 
                    producto.getPrecio(), 
                    producto.getPorcentajeGanancia(),
                    cantidadDeseada, 
                    0
                );
                productoCarrito.calcularPrecioVenta();

                // Actualizar el carrito DTO
                if (carritoDTO == null) {
                    carritoDTO = new CarritoDTO(new ArrayList<>(), 0);
                }
                carritoDTO.getProductos().add(productoCarrito);
                carritoDTO.actualizarTotal();

                Platform.runLater(() -> {
                    cargarProductos();
                    mostrarAlerta("Éxito", "Producto agregado al carrito.", Alert.AlertType.INFORMATION);
                });

            } catch (NumberFormatException e) {
                String mensaje = e.getMessage() != null && e.getMessage().contains("mayor a cero") 
                    ? "La cantidad debe ser un número mayor a cero" 
                    : "Ingrese un número válido.";
                mostrarAlerta("Error", mensaje, Alert.AlertType.ERROR);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al procesar la venta", e);
                mostrarAlerta("Error", "Ocurrió un error al procesar la venta: " + e.getMessage(), 
                            Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void btnIrCarritoAction(ActionEvent event) {
        if (carritoDTO == null || carritoDTO.getProductos().isEmpty()) {
            mostrarAlerta("Carrito Vacío", "No hay productos en el carrito.", Alert.AlertType.INFORMATION);
            return;
        }

        // Mostrar resumen antes de ir al carrito
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resumen del Carrito");
        alert.setHeaderText("Resumen de productos en el carrito");

        StringBuilder contenido = new StringBuilder("Productos en el carrito:\n\n");
        double total = 0;

        for (Productos p : carritoDTO.getProductos()) {
            double subtotal = p.getPrecioParaVender() * p.getCantidad();
            contenido.append(String.format("%s x%d - $%,.2f\n", 
                p.getNombre(), p.getCantidad(), subtotal));
            total += subtotal;
        }

        contenido.append(String.format("\nTotal: $%,.2f\n\n¿Desea proceder al pago?", total));
        alert.setContentText(contenido.toString());

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent()) {
            if (resultado.get() == ButtonType.YES) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/pedido-view.fxml"));
                    Parent root = loader.load();

                    // Pasar el carrito al controlador de pedido
                    PedidoController pedidoController = loader.getController();
                    pedidoController.setCarritoDTO(carritoDTO);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error al cargar la vista de pedido", e);
                    mostrarAlerta("Error", "No se pudo cargar la vista de pedido: " + e.getMessage(), 
                                 Alert.AlertType.ERROR);
                }
            } else if (resultado.get() == ButtonType.NO || resultado.get() == ButtonType.CANCEL) {
                // Devolver productos al inventario
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
                // Actualizar la vista
                cargarProductos();
                mostrarAlerta("Operación cancelada", "Los productos han sido devueltos al inventario.", Alert.AlertType.INFORMATION);
            }
            // Si es CANCEL, no hacer nada
        }
    }

    private void cambiarVentana(ActionEvent event, String fxmlFile, String title) {
        ViewLoader.cargarVista(event, fxmlFile, title);
    }
}
