package LogicaTienda.Forms;

import LogicaTienda.Model.Productos;
import LogicaTienda.Services.ProductoService;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class formularioProduct extends Stage {
    private TextField idProductoField, nombreField, precioField, porcentajeGananciaField, cantidadField;
    private Button submitButton;
    private final ObservableList<Productos> productos;
    private static final Logger LOGGER = Logger.getLogger(formularioProduct.class.getName());

    public formularioProduct(String titulo, ObservableList<Productos> productos, boolean esEliminacion, Object unused, Productos producto) {
        this.productos = productos;
        setTitle(titulo);
        initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        idProductoField = new TextField();
        nombreField = new TextField();
        precioField = new TextField();
        porcentajeGananciaField = new TextField();
        cantidadField = new TextField();

        int row = 0;
        boolean esNuevo = producto == null && !esEliminacion;

        if (esNuevo) {
            Label infoId = new Label("El ID se generará automáticamente al guardar.");
            grid.add(infoId, 0, row++, 2, 1);
        } else {
            grid.add(new Label("ID Producto:"), 0, row);
            grid.add(idProductoField, 1, row++);
        }

        grid.add(new Label("Nombre:"), 0, row);
        grid.add(nombreField, 1, row++);

        if (!esEliminacion) {
            grid.add(new Label("Precio:"), 0, row);
            grid.add(precioField, 1, row++);
            grid.add(new Label("% Ganancia:"), 0, row);
            grid.add(porcentajeGananciaField, 1, row++);
            grid.add(new Label("Cantidad:"), 0, row);
            grid.add(cantidadField, 1, row++);
        }

        submitButton = new Button(esEliminacion ? "Eliminar" : "Guardar");
        grid.add(submitButton, 1, row);

        // Modo edición: mostrar ID, pero no permitir modificarlo.
        if (producto != null) {
            idProductoField.setText(producto.getIdProducto());
            nombreField.setText(producto.getNombre());
            precioField.setText(String.valueOf(producto.getPrecio()));
            porcentajeGananciaField.setText(String.valueOf(producto.getPorcentajeGanancia()));
            cantidadField.setText(String.valueOf(producto.getCantidad()));
            idProductoField.setEditable(false);
            idProductoField.setDisable(true);
        }

        // Si se está eliminando sin producto cargado, solo permitir editar el ID.
        if (esEliminacion) {
            nombreField.setDisable(true);
            precioField.setDisable(true);
            porcentajeGananciaField.setDisable(true);
            cantidadField.setDisable(true);
            if (idProductoField != null) {
                idProductoField.setEditable(true);
                idProductoField.setDisable(false);
            }
        }

        submitButton.setOnAction(e -> {
            try {
                boolean cambiosRealizados = esEliminacion ? eliminarProducto() : actualizarOAgregarProducto(producto);
                if (cambiosRealizados) {
                    mostrarAlerta("Éxito", "Operación realizada correctamente.", Alert.AlertType.INFORMATION);
                    close();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error en la operación del formulario", ex);
                mostrarAlerta("Error", "Ocurrió un error: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        // Aumentamos un poco la altura del formulario para que quede más cómodo (aprox +30%)
        int baseWidth = 380;
        // El usuario pidió "por ahi 3 de tamaño" — interpretamos como aumentar la altura x3 respecto
        // a los valores base originales (230 y 250).
        int heightNuevo = 280;
        int heightEdicion = 280 ;
        setScene(new Scene(grid, baseWidth, esNuevo ? heightNuevo : heightEdicion));
    }

    private boolean actualizarOAgregarProducto(Productos productoExistente) {
        String nombre = nombreField.getText().trim();
        if (nombre.isEmpty()) {
            return mostrarError("El nombre no puede estar vacío.");
        }

        if (!validarCamposNumericos(precioField, cantidadField)) {
            return false;
        }

        double precio = Double.parseDouble(precioField.getText().trim());
        double porcentajeGanancia = 20.0;
        String porcentajeText = porcentajeGananciaField.getText().trim();
        if (!porcentajeText.isEmpty()) {
            try {
                porcentajeGanancia = Double.parseDouble(porcentajeText);
                if (porcentajeGanancia < 0) {
                    return mostrarError("El porcentaje de ganancia no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                return mostrarError("El porcentaje de ganancia debe ser un número válido.");
            }
        }

        int cantidad = Integer.parseInt(cantidadField.getText().trim());

        try {
            if (productoExistente != null) {
                productoExistente.setNombre(nombre);
                productoExistente.setPrecio(precio);
                productoExistente.setPorcentajeGanancia(porcentajeGanancia);
                productoExistente.setCantidad(cantidad);
                productoExistente.calcularPrecioVenta();

                try {
                    ProductoService.actualizarProducto(productoExistente);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al actualizar el producto", e);
                    return mostrarError("No se pudo actualizar el producto en la base de datos: " + e.getMessage());
                }
            } else {
                Productos nuevoProducto = new Productos("", nombre, precio, porcentajeGanancia, cantidad, 0);
                nuevoProducto.calcularPrecioVenta();

                try {
                    ProductoService.guardarProducto(nuevoProducto);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al guardar el producto", e);
                    return mostrarError("No se pudo guardar el producto en la base de datos: " + e.getMessage());
                }

                if (productos != null) {
                    productos.add(nuevoProducto);
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar el producto", e);
            return mostrarError("Error al guardar el producto: " + e.getMessage());
        }
    }

    private boolean eliminarProducto() {
        String idProducto = idProductoField != null ? idProductoField.getText().trim() : "";
        if (idProducto.isEmpty()) {
            return mostrarError("Debes ingresar un ID de producto válido.");
        }

        try {
            if (ProductoService.buscarProductoPorId(idProducto) == null) {
                return mostrarError("No se encontró un producto con ese ID.");
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText("¿Está seguro de eliminar este producto?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                ProductoService.eliminarProducto(idProducto);
                if (productos != null) {
                    productos.removeIf(p -> p.getIdProducto().equals(idProducto));
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar el producto", e);
            return mostrarError("Error al eliminar el producto: " + e.getMessage());
        }
    }

    private boolean validarCamposNumericos(TextField precioField, TextField cantidadField) {
        try {
            if (precioField.getText().trim().isEmpty() ||
                cantidadField.getText().trim().isEmpty()) {
                return mostrarError("Todos los campos numéricos son obligatorios.");
            }

            double precio = Double.parseDouble(precioField.getText().trim());
            double porcentajeGanancia = 0;
            if (!porcentajeGananciaField.getText().trim().isEmpty()) {
                porcentajeGanancia = Double.parseDouble(porcentajeGananciaField.getText().trim());
            }
            int cantidad = Integer.parseInt(cantidadField.getText().trim());

            if (precio < 0 || porcentajeGanancia < 0 || cantidad < 0) {
                return mostrarError("Los valores numéricos deben ser positivos.");
            }
            return true;
        } catch (NumberFormatException e) {
            return mostrarError("Datos inválidos. Verifica los valores numéricos.");
        }
    }

    private boolean mostrarError(String mensaje) {
        mostrarAlerta("Error", mensaje, Alert.AlertType.ERROR);
        return false;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarAlerta(titulo, mensaje, Alert.AlertType.INFORMATION);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        try {
            Alert alerta = new Alert(tipo);
            alerta.setTitle(titulo);
            alerta.setHeaderText(null);
            alerta.setContentText(mensaje);
            alerta.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al mostrar la alerta", e);
            System.err.println(titulo + ": " + mensaje);
        }
    }
}
