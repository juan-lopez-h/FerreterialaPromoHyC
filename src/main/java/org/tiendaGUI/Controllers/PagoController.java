package org.tiendaGUI.Controllers;

import LogicaTienda.Model.Factura;
import LogicaTienda.Model.Productos;
import LogicaTienda.Services.FacturaService;
import javafx.util.Pair;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.awt.Desktop;
import LogicaTienda.Services.ProductoService;

import javafx.scene.control.Button;
import javafx.scene.control.cell.TextFieldTableCell;
import org.tiendaGUI.DTO.CarritoDTO;
import javax.swing.*;

import LogicaTienda.Model.Pago;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tiendaGUI.utils.PDFGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.tiendaGUI.Controllers.loader.ViewLoader.mostrarAlerta;

public class PagoController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(PagoController.class.getName());
    private final PDFGenerator pdfGenerator = new PDFGenerator();
    @FXML private Label lblTotalCarrito;
    @FXML private Label lblCambio;
    @FXML private Label lblMontoRequerido;
    @FXML private TextField montoField;
    @FXML private ComboBox<String> metodoPagoCombo;
    @FXML private TextField referenciaField;
    @FXML private Button btnProcesar;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, String> columnaId;
    @FXML private TableColumn<Pago, Double> columnaMonto;
    @FXML private TableColumn<Pago, String> columnaMetodo;
    @FXML private TableColumn<Pago, String> columnaFecha;
    @FXML private TableColumn<Pago, String> columnaEstado;
    @FXML private TableColumn<Pago, String> columnaReferencia;

    // DTO recibido desde PedidoController
    private CarritoDTO carritoDTO;
    private double montoTotalCarrito = 0.0;

    @FXML
    public void setCarritoDTO(CarritoDTO carritoDTO) {
        this.carritoDTO = carritoDTO;
        // Inicializar monto y tabla de carrito en base al DTO
        this.montoTotalCarrito = carritoDTO.getTotal();
        actualizarInterfazMonto(montoTotalCarrito);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas de tabla de pagos
        columnaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnaMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        columnaMetodo.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columnaReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
        columnaId.setCellFactory(TextFieldTableCell.forTableColumn());
        columnaId.setOnEditCommit(event -> {
            Pago pago = event.getRowValue();
            pago.setId(event.getNewValue());
        });
        // Cargar pagos actuales - Usando lista local en lugar de DataModel
        ObservableList<Pago> pagos = FXCollections.observableArrayList();
        tablaPagos.setItems(pagos);
        tablaPagos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // Configurar combo de métodos
        metodoPagoCombo.setItems(FXCollections.observableArrayList(
                "Efectivo", "Tarjeta", "Transferencia", "Otro"
        ));
        metodoPagoCombo.setValue("Transferencia");

        // Formato numérico para monto
        montoField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));

        // Listeners
        montoField.textProperty().addListener((obs, oldV, newV) -> {
            try {
                double ingresado = newV.isEmpty() ? 0 : Double.parseDouble(newV);
                actualizarCambio(ingresado);
            } catch (NumberFormatException ignored) {}
        });
        metodoPagoCombo.valueProperty().addListener((obs, o, n) -> {
            boolean esEfectivo = "Efectivo".equals(n);
            referenciaField.setDisable(esEfectivo);
            if (esEfectivo) referenciaField.clear();
        });

        lblCambio.setText("Cambio: $0.00");
    }

    @FXML
    private void btnProcesarAction() {
        try {
            // Validaciones básicas
            String text = montoField.getText();
            if (text.isEmpty()) { 
                showError("Debe ingresar un monto"); 
                return; 
            }

            double ingresado = Double.parseDouble(text);
            if (ingresado < montoTotalCarrito) { 
                showError("Monto insuficiente"); 
                return; 
            }

            String metodo = metodoPagoCombo.getValue();
            if (!"Efectivo".equals(metodo) && referenciaField.getText().trim().isEmpty()) {
                showError("Referencia requerida para " + metodo); 
                return;
            }

            double cambio = ingresado - montoTotalCarrito;

            // Preguntar si es cotización o pago
            int opcion = JOptionPane.showOptionDialog(
                null,
                "¿Desea generar una cotización o realizar el pago?",
                "Seleccione una opción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Cotización", "Pago"},
                "Pago"
            );

            boolean esCotizacion = (opcion == 0); // 0 = Cotización, 1 = Pago

            // Registrar el pago y actualizar el inventario (solo si no es cotización)
            Pago pago = registrarPago(ingresado, cambio, esCotizacion);

            // Crear factura con diálogo
            crearFacturaConDialogo(pago, esCotizacion);

            // Mostrar mensaje de éxito
            if (!esCotizacion) {
                showInfo(String.format("Pago correcto: pagó $%.2f, cambio $%.2f", ingresado, cambio));
                limpiarCampos();
            } else {
                showInfo("Cotización generada correctamente");
            }

        } catch (NumberFormatException e) {
            showError("Por favor ingrese un monto válido");
            LOGGER.log(Level.WARNING, "Formato de monto inválido", e);
        } catch (Exception e) {
            showError("Error al procesar el pago: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error en btnProcesarAction", e);
        }
    }

    @FXML
    private void btnCancelarAction() {
        limpiarCampos();
    }

    @FXML
    private void btnVolverAction(ActionEvent event) {
        // Al volver, devolver los productos al inventario y pasar el mismo DTO de carrito
        try {
            // Devolver los productos al inventario
            if (carritoDTO != null && carritoDTO.getProductos() != null) {
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
            }

            // Cargar la vista del carrito
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/pedido-view.fxml"));
            Parent root = loader.load();
            PedidoController ctrl = loader.getController();
            ctrl.setCarritoDTO(carritoDTO);
            Stage s = (Stage)((Node)event.getSource()).getScene().getWindow();
            s.setScene(new Scene(root)); s.setTitle("Carrito de Compras"); s.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver a la pantalla de carrito", e);
        }
    }

    private Pago registrarPago(double montoPagado, double cambio) {
        return registrarPago(montoPagado, cambio, false);
    }

    private Pago registrarPago(double montoPagado, double cambio, boolean esCotizacion) {
        try {
            String idPago = UUID.randomUUID().toString().substring(0,8);
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String metodo = metodoPagoCombo.getValue();
            String ref = "Efectivo".equals(metodo)
                    ? String.format("Pagado $%.2f, cambio $%.2f", montoPagado, cambio)
                    : referenciaField.getText().trim();

            // Crear el pago
            Pago pago = new Pago(idPago, montoTotalCarrito, metodo, fecha, "Completado", ref);

            // Actualizar el inventario para cada producto en el carrito (solo si no es cotización)
            if (!esCotizacion) {
                for (Productos producto : carritoDTO.getProductos()) {
                    try {
                        ProductoService.actualizarStock(
                            producto.getIdProducto(),
                            producto.getCantidad(),
                            true // true para restar del stock (venta)
                        );
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error al actualizar el stock del producto: " + producto.getIdProducto(), e);
                        throw new RuntimeException("Error al actualizar el inventario: " + e.getMessage(), e);
                    }
                }
            }

            // Actualizar la tabla de pagos
            if (tablaPagos.getItems() != null) {
                tablaPagos.getItems().add(pago);
                tablaPagos.refresh();
            }

            return pago;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al registrar el pago", e);
            throw new RuntimeException("Error al registrar el pago: " + e.getMessage(), e);
        }
    }
    public void crearFacturaConDialogo(Pago pago, boolean esCotizacion) {
        try {
            // Crear diálogo para ingresar los datos del cliente
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Datos del Cliente");
            dialog.setHeaderText("Ingrese los datos del cliente para la " + (esCotizacion ? "cotización" : "factura"));

            // Configurar los botones
            ButtonType btnGenerarFactura = new ButtonType(esCotizacion ? "Generar Cotización" : "Generar Factura", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnGenerarFactura, ButtonType.CANCEL);

            // Crear los campos del formulario
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField nombreField = new TextField();
            nombreField.setPromptText("Nombre completo");

            // ComboBox para tipo de documento
            ComboBox<String> tipoDocCombo = new ComboBox<>();
            tipoDocCombo.getItems().addAll("Cédula de ciudadanía", "Cédula de extranjería", "NIT", "Pasaporte", "Tarjeta de identidad");
            tipoDocCombo.setValue("Cédula de ciudadanía");

            TextField cedulaField = new TextField();
            cedulaField.setPromptText("Número de identificación");
            TextField emailField = new TextField();
            emailField.setPromptText("Correo electrónico (opcional)");
            TextField telefonoField = new TextField();
            telefonoField.setPromptText("Teléfono (opcional)");

            // Añadir campos al grid
            grid.add(new Label("Nombre completo:"), 0, 0);
            grid.add(nombreField, 1, 0);
            grid.add(new Label("Tipo de documento:"), 0, 1);
            grid.add(tipoDocCombo, 1, 1);
            grid.add(new Label("Número de documento:"), 0, 2);
            grid.add(cedulaField, 1, 2);
            grid.add(new Label("Email:"), 0, 3);
            grid.add(emailField, 1, 3);
            grid.add(new Label("Teléfono:"), 0, 4);
            grid.add(telefonoField, 1, 4);

            // Validar campos requeridos
            Node btnGenerar = dialog.getDialogPane().lookupButton(btnGenerarFactura);
            btnGenerar.setDisable(true);

            // Validación en tiempo real
            ChangeListener<String> validacionListener = (observable, oldValue, newValue) -> {
                boolean camposValidos = !nombreField.getText().trim().isEmpty() &&
                        !cedulaField.getText().trim().isEmpty();
                btnGenerar.setDisable(!camposValidos);
            };

            nombreField.textProperty().addListener(validacionListener);
            cedulaField.textProperty().addListener(validacionListener);

            dialog.getDialogPane().setContent(grid);

            // Convertir el resultado a un objeto que contenga todos los datos del cliente
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == btnGenerarFactura) {
                    return new Pair<>(
                        nombreField.getText().trim(),
                        String.format("%s: %s", 
                            tipoDocCombo.getValue(), 
                            cedulaField.getText().trim())
                    );
                }
                return null;
            });

            // Mostrar diálogo y procesar resultado
            Optional<Pair<String, String>> resultado = dialog.showAndWait();

            if (resultado.isPresent()) {
                Pair<String, String> clienteData = resultado.get();
                String nombreCliente = clienteData.getKey();
                String tipoYNumeroDoc = clienteData.getValue();
                String emailCliente = emailField.getText().trim();
                String telefonoCliente = telefonoField.getText().trim();
                String tipoDocumento = tipoDocCombo.getValue();
                String numeroDocumento = cedulaField.getText().trim();

                // Validar formato del número de documento
                if (!numeroDocumento.matches("^\\d+$")) {
                    showError("El número de documento debe contener solo dígitos numéricos");
                    return;
                }


                // Crear la factura en MongoDB
                List<Productos> productosFactura = new ArrayList<>(carritoDTO.getProductos());

                // Crear la factura
                Factura factura = new Factura();
                factura.setId(UUID.randomUUID().toString().substring(0, 8));
                factura.setClienteNombre(nombreCliente);
                factura.setClienteIdentificacion(String.format("%s: %s", tipoDocumento, numeroDocumento));
                factura.setClienteEmail(emailCliente);
                factura.setClienteTelefono(telefonoCliente);
                factura.setProductos(productosFactura);
                factura.setFecha(LocalDateTime.now());
                factura.setTotal(montoTotalCarrito);
                factura.setMetodoPago(pago.getMetodoPago());
                factura.setReferenciaPago(pago.getReferencia());
                factura.setTipoDocumento(tipoDocumento);
                factura.setTipoFactura(esCotizacion ? "Cotizacion" : "Impresa");

                try {
                    // Guardar la factura en la base de datos
                    String facturaId = FacturaService.crearFactura(
                        productosFactura,
                        nombreCliente,
                        numeroDocumento, // Solo el número de documento
                        montoTotalCarrito,
                        pago.getMetodoPago(),
                        pago.getReferencia(),
                        emailCliente,
                        telefonoCliente,
                        tipoDocumento, // Tipo de documento (ej: "Cédula de ciudadanía")
                        esCotizacion ? "Cotizacion" : "Impresa" // Tipo de factura
                    );
                    factura.setId(facturaId);

                    try {
                        // Generar PDF de la factura
                        PDFGenerator.generarFacturaPDF(factura);

                        // Mostrar mensaje de éxito con opción para abrir la factura o cotización
                        Alert alert = getAlert(esCotizacion, facturaId, nombreCliente);

                        // Agregar botón para abrir la factura o cotización
                        ButtonType btnAbrirFactura = new ButtonType("Abrir " + (esCotizacion ? "Cotización" : "Factura"), ButtonBar.ButtonData.YES);
                        ButtonType btnCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(btnAbrirFactura, btnCerrar);

                        // Mostrar diálogo y manejar la respuesta
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == btnAbrirFactura) {
                            // Intentar abrir el archivo PDF con el visor predeterminado
                            String userHome = System.getProperty("user.home");
                            File downloads = new File(userHome, "Downloads");
                            if (!downloads.exists() || !downloads.isDirectory()) {
                                downloads = new File(userHome, "Descargas");
                            }
                            String fileName = esCotizacion ? "COTIZACION_" + facturaId + ".pdf" : "FACTURA_" + facturaId + ".pdf";
                            File pdfFile = new File(downloads, fileName);

                            if (pdfFile.exists()) {
                                try {
                                    Desktop.getDesktop().open(pdfFile);
                                } catch (IOException ex) {
                                    LOGGER.log(Level.WARNING, "No se pudo abrir el archivo PDF", ex);
                                    showInfo("La factura se guardó en: " + pdfFile.getAbsolutePath());
                                }
                            }
                        }

                        // Limpiar el carrito después del pago exitoso (solo si no es cotización)
                        if (!esCotizacion) {
                            carritoDTO.getProductos().clear();
                            carritoDTO.actualizarTotal();
                        } else {
                            // Si es una cotización, devolver los productos al inventario
                            // ya que no es una venta efectiva
                            for (Productos producto : productosFactura) {
                                ProductoService.actualizarStock(producto.getIdProducto(), producto.getCantidad(), false);
                            }
                            LOGGER.info("Productos de cotización devueltos al inventario");
                        }

                    } catch (IOException ioEx) {
                        LOGGER.log(Level.SEVERE, "Error al generar el PDF de la factura", ioEx);
                        showError(String.format(
                            "La factura se creó correctamente (ID: %s) pero hubo un error al generar el PDF: %s",
                            facturaId, ioEx.getMessage()
                        ));
                    }

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al crear la factura", e);
                    showError("Error al generar la factura: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado en crearFacturaConDialogo", e);
            showError("Error inesperado: " + e.getMessage());
        }
    }

    private Alert getAlert(boolean esCotizacion, String facturaId, String nombreCliente) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(esCotizacion ? "Cotización Generada" : "Factura Generada");
        alert.setHeaderText(esCotizacion ? "¡Cotización generada exitosamente!" : "¡Factura generada exitosamente!");
        alert.setContentText(String.format("Número de %s: %s\nCliente: %s\nTotal: $%,.2f",
            esCotizacion ? "cotización" : "factura", facturaId, nombreCliente, montoTotalCarrito));
        return alert;
    }

    private void actualizarCambio(double ingresado) {
        double dif = ingresado - montoTotalCarrito;
        lblCambio.setText(String.format(dif>=0?"Cambio: $%.2f":"Falta: $%.2f", Math.abs(dif)));
        lblCambio.setStyle(dif>=0?"":"-fx-text-fill:red");
        new FadeTransition(Duration.millis(200), lblCambio).play();
    }

    private void actualizarInterfazMonto(double monto) {
        Platform.runLater(() -> {
            lblTotalCarrito.setText(String.format("Total: $%.2f", monto));
            lblMontoRequerido.setText(String.format("Debe: $%.2f", monto));
        });
    }

    private void limpiarCampos() {
        montoField.clear(); referenciaField.clear(); metodoPagoCombo.setValue("Efectivo"); lblCambio.setText("Cambio: $0.00");
    }

    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
    private void showInfo(String msg)  { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }

    public void imprimirPDF(ActionEvent actionEvent) {
        Factura facturaSeleccionada = obtenerFacturaDesdePagoSeleccionado();

        if (facturaSeleccionada == null) {
            mostrarAlerta("cuidado","Debes seleccionar un pago relacionado a una factura.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Obtener la ruta de la carpeta de Descargas
            String userHome = System.getProperty("user.home");
            File downloadsDir = new File(userHome, "Downloads");

            // Si no existe la carpeta Downloads, intentar con Descargas (español)
            if (!downloadsDir.exists() || !downloadsDir.isDirectory()) {
                downloadsDir = new File(userHome, "Descargas");
                // Si tampoco existe, crear la carpeta
                if (!downloadsDir.exists()) {
                    boolean created = downloadsDir.mkdirs();
                    if (!created) {
                        throw new IOException("No se pudo crear la carpeta de descargas");
                    }
                }
            }

            // Crear el nombre del archivo con timestamp
            String timeStamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            );
            String fileName = String.format("Factura_%s_%s.pdf", facturaSeleccionada.getId(), timeStamp);
            File pdfFile = new File(downloadsDir, fileName);

            // Generar el PDF directamente en la carpeta de descargas
            PDFGenerator.generarFacturaPDF(facturaSeleccionada);

            // Mostrar mensaje de éxito con la ruta del archivo
            mostrarAlerta("✅ Success", "✅ Factura descargada exitosamente en: " + pdfFile.getAbsolutePath(), Alert.AlertType.INFORMATION);

            // Abrir el archivo automáticamente si es posible
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(pdfFile);
                } catch (Exception e) {
                    // Si no se puede abrir, solo mostramos el mensaje de éxito
                }
            }
        } catch (Exception e) {
            mostrarAlerta("❌ Error","❌ Error al guardar la factura: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Factura obtenerFacturaDesdePagoSeleccionado() {
        Pago pago = tablaPagos.getSelectionModel().getSelectedItem();
        if (pago == null) {
            LOGGER.warning("No se ha seleccionado ningún pago");
            return null;
        }

        try {
            // Buscar factura por ID de pago
            // Nota: Esto asume que el ID del pago es el mismo que el ID de factura
            // Si hay una relación diferente, deberías ajustar este código
            // para buscar por el campo correcto en la colección de facturas
            Factura factura = FacturaService.buscarFacturaPorId(pago.getId());

            if (factura == null) {
                LOGGER.warning("No se encontró factura para el pago con ID: " + pago.getId());
            }
            return factura;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar factura para el pago con ID: " + pago.getId(), e);
            return null;
        }
    }

}
