package org.tiendaGUI.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import LogicaTienda.Model.Factura;
import LogicaTienda.Model.Productos;
import LogicaTienda.Services.FacturaService;
import LogicaTienda.Services.ProductoService;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleEstadisticasController {
    // UI Components
    @FXML private Label lblTotalVentas;
    @FXML private Label lblTotalInventario;
    @FXML private Label lblProductoMasVendido;
    @FXML private Label lblTotalFacturas;
    @FXML private Label lblPromedioVenta;
    @FXML private Label lblProductosVendidos;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private BarChart<String, Number> graficoBarras;
    @FXML private PieChart graficoPastel;
    @FXML private TableView<Productos> tablaProductos;
    @FXML private TableColumn<Productos, String> colProducto;
    @FXML private TableColumn<Productos, Number> colCantidad;
    @FXML private TableColumn<Productos, String> colValor;
    @FXML private VBox contenedorGraficos;

    // Data
    private List<Factura> facturas;
    private List<Productos> productos;
    
    // Formatters
    private NumberFormat currencyFormat;
    private NumberFormat numberFormat;
    private DateTimeFormatter dateFormatter;

    @FXML
    public void initialize() {
        try {
            // Inicializar formatos
            currencyFormat = NumberFormat.getCurrencyInstance();
            numberFormat = NumberFormat.getNumberInstance();
            dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            // Configurar fechas
            configurarFechas();
            
            // Cargar datos
            cargarDatos();
            
            // Configurar tabla
            configurarTabla();
            
            // Actualizar estadísticas iniciales
            actualizarEstadisticas();
            
            // Configurar listeners para actualización automática
            dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> actualizarEstadisticas());
            dateFin.valueProperty().addListener((obs, oldDate, newDate) -> actualizarEstadisticas());
            
        } catch (Exception e) {
            mostrarError("Error al inicializar la ventana de estadísticas: " + e.getMessage());
        }
    }

    private void configurarFechas() {
        try {
            LocalDate hoy = LocalDate.now();
            dateInicio.setValue(hoy.withDayOfMonth(1)); // Primer día del mes
            dateFin.setValue(hoy); // Hoy

            // Configurar restricciones de fecha para dateInicio
            dateInicio.setDayCellFactory(picker -> new DateCell() {
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate hoy = LocalDate.now();
                    setDisable(empty || date == null || date.isAfter(hoy));
                    
                    // Resaltar fechas fuera de rango
                    if (date != null && date.isAfter(hoy)) {
                        setStyle("-fx-background-color: #ffc0cb;");
                    }
                }
            });

            // Configurar restricciones de fecha para dateFin
            dateFin.setDayCellFactory(picker -> new DateCell() {
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate hoy = LocalDate.now();
                    LocalDate fechaInicio = dateInicio.getValue();
                    
                    // Deshabilitar fechas posteriores a hoy o anteriores a la fecha de inicio
                    boolean deshabilitar = empty || date == null || 
                                         date.isAfter(hoy) || 
                                         (fechaInicio != null && date.isBefore(fechaInicio));
                    
                    setDisable(deshabilitar);
                    
                    // Resaltar fechas fuera de rango
                    if (date != null && (date.isAfter(hoy) || 
                        (fechaInicio != null && date.isBefore(fechaInicio)))) {
                        setStyle("-fx-background-color: #ffc0cb;");
                    }
                }
            });
            
            // Actualizar dateFin cuando cambia dateInicio
            dateInicio.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (dateFin.getValue() == null || dateFin.getValue().isBefore(newValue)) {
                        dateFin.setValue(newValue);
                    }
                    dateFin.setDayCellFactory(picker -> new DateCell() {
                        public void updateItem(LocalDate date, boolean empty) {
                            super.updateItem(date, empty);
                            LocalDate hoy = LocalDate.now();
                            boolean deshabilitar = empty || date == null || 
                                                 date.isAfter(hoy) || date.isBefore(newValue);
                            setDisable(deshabilitar);
                            
                            if (date != null && (date.isAfter(hoy) || date.isBefore(newValue))) {
                                setStyle("-fx-background-color: #ffc0cb;");
                            }
                        }
                    });
                }
            });
            
        } catch (Exception e) {
            mostrarError("Error al configurar fechas: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            facturas = FacturaService.obtenerTodasLasFacturas();
            productos = ProductoService.obtenerTodosLosProductos();
        } catch (Exception e) {
            mostrarError("Error al cargar los datos: " + e.getMessage());
        }
    }

    private void configurarTabla() {
        try {
            // Configurar columna de producto
            colProducto.setCellValueFactory(cellData -> {
                try {
                    return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre());
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("");
                }
            });

            // Configurar columna de cantidad
            colCantidad.setCellValueFactory(cellData -> {
                try {
                    return new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCantidad());
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleIntegerProperty(0);
                }
            });

            // Configurar columna de valor total
            colValor.setCellValueFactory(cellData -> {
                try {
                    double valorTotal = cellData.getValue().getPrecio() * cellData.getValue().getCantidad();
                    return new javafx.beans.property.SimpleStringProperty(currencyFormat.format(valorTotal));
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty(currencyFormat.format(0));
                }
            });

            // Ordenar por cantidad descendente
            colCantidad.setSortType(TableColumn.SortType.DESCENDING);
            tablaProductos.getSortOrder().add(colCantidad);
        } catch (Exception e) {
            mostrarError("Error al configurar la tabla: " + e.getMessage());
        }
    }

    @FXML
    private void onBuscarClick() {
        actualizarEstadisticas();
    }

    @FXML
    private void onHoyClick() {
        try {
            LocalDate hoy = LocalDate.now();
            dateInicio.setValue(hoy);
            dateFin.setValue(hoy);
            actualizarEstadisticas();
        } catch (Exception e) {
            mostrarError("Error al seleccionar la fecha de hoy: " + e.getMessage());
        }
    }

    @FXML
    private void onVolverClick() {
        try {
            // Obtener la escena actual y la ventana
            Stage stage = (Stage) dateInicio.getScene().getWindow();
            
            // Cargar la vista principal (hello-view.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/hello-view.fxml"));
            Parent root = loader.load();
            
            // Configurar la escena
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Ferretería La Promo");
            stage.centerOnScreen();
            
        } catch (Exception e) {
            mostrarError("Error al intentar volver a la ventana principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarEstadisticas() {
        try {
            LocalDate inicio = dateInicio.getValue();
            LocalDate fin = dateFin.getValue();
            LocalDate hoy = LocalDate.now();

            // Validaciones de fecha
            if (inicio == null || fin == null) {
                mostrarError("Por favor seleccione ambas fechas");
                return;
            }
            
            if (fin.isBefore(inicio)) {
                mostrarError("La fecha final no puede ser anterior a la fecha inicial");
                return;
            }
            
            if (inicio.isAfter(hoy) || fin.isAfter(hoy)) {
                mostrarError("No se pueden seleccionar fechas futuras");
                dateInicio.setValue(hoy);
                dateFin.setValue(hoy);
                return;
            }

            // Asegurarse de incluir todo el día de la fecha final
            List<Factura> facturasFiltradas = filtrarFacturasPorFecha(inicio, fin.plusDays(1));
            
            // Actualizar la interfaz de usuario
            actualizarMetricas(facturasFiltradas);
            actualizarGraficoBarras(facturasFiltradas);
            actualizarGraficoPastel(facturasFiltradas);
            actualizarTablaProductos();
            
            // Mostrar mensaje de éxito
            mostrarInfo(
                    String.format("Mostrando resultados del %s al %s (%d facturas encontradas)",
                    inicio.format(dateFormatter),
                    fin.format(dateFormatter),
                    facturasFiltradas.size()));
                    
        } catch (Exception e) {
            mostrarError("Error al actualizar estadísticas: " + e.getMessage());
        }
    }

    private List<Factura> filtrarFacturasPorFecha(LocalDate inicio, LocalDate fin) {
        return facturas.stream()
                .filter(Objects::nonNull)
                .filter(f -> f.getFecha() != null)
                .filter(f -> !f.getFecha().toLocalDate().isBefore(inicio))
                .filter(f -> !f.getFecha().toLocalDate().isAfter(fin))
                .collect(Collectors.toList());
    }

    private void actualizarMetricas(List<Factura> facturasFiltradas) {
        try {
            // Calcular total de ventas
            double totalVentas = facturasFiltradas.stream()
                    .filter(Objects::nonNull)
                    .mapToDouble(Factura::getTotal)
                    .sum();

            // Calcular valor total del inventario
            double totalInventario = productos.stream()
                    .filter(Objects::nonNull)
                    .mapToDouble(p -> p.getPrecio() * p.getCantidad())
                    .sum();

            // Actualizar etiquetas
            lblTotalVentas.setText(currencyFormat.format(totalVentas));
            lblTotalInventario.setText(currencyFormat.format(totalInventario));
            lblTotalFacturas.setText("Facturas: " + facturasFiltradas.size());

            // Calcular promedio de venta
            double promedioVenta = facturasFiltradas.isEmpty() ? 0 : totalVentas / facturasFiltradas.size();
            lblPromedioVenta.setText("Venta promedio: " + currencyFormat.format(promedioVenta));

            // Encontrar producto con más existencias
            productos.stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getCantidad() > 0)
                    .max(Comparator.comparingInt(Productos::getCantidad))
                    .ifPresentOrElse(
                        p -> lblProductoMasVendido.setText(String.format("%s (%d unidades)", 
                            p.getNombre(), p.getCantidad())),
                        () -> lblProductoMasVendido.setText("Sin existencias")
                    );

            // Contar productos vendidos
            long totalProductosVendidos = facturasFiltradas.stream()
                    .filter(Objects::nonNull)
                    .flatMap(f -> f.getProductos().stream())
                    .filter(Objects::nonNull)
                    .count();
            
            lblProductosVendidos.setText("Productos vendidos: " + numberFormat.format(totalProductosVendidos));
            
        } catch (Exception e) {
            mostrarError("Error al actualizar métricas: " + e.getMessage());
        }
    }

    private void actualizarGraficoBarras(List<Factura> facturas) {
        try {
            // Limpiar datos y configurar scroll
            graficoBarras.getData().clear();
            
            if (facturas == null || facturas.isEmpty()) {
                return;
            }

            // Agrupar ventas por día
            Map<LocalDate, Double> ventasPorDia = facturas.stream()
                .filter(Objects::nonNull)
                .filter(f -> f.getFecha() != null)
                .collect(Collectors.groupingBy(
                    f -> f.getFecha().toLocalDate(),
                    TreeMap::new,
                    Collectors.summingDouble(Factura::getTotal)
                ));

            // Crear series para el gráfico
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ventas por Día");

            // Encontrar valores máximos y mínimos
            double maxValorLocal = 0;
            double minValorLocal = Double.MAX_VALUE;
            for (Double valor : ventasPorDia.values()) {
                if (valor > maxValorLocal) maxValorLocal = valor;
                if (valor < minValorLocal) minValorLocal = valor;
            }

            // Calcular rango y variación
            double rangoLocal = maxValorLocal - minValorLocal;
            double variacionLocal = (rangoLocal > 0) ? (maxValorLocal / rangoLocal) : 1;

            // Agregar datos al gráfico
            ventasPorDia.forEach((fecha, total) -> {
                String fechaFormateada = fecha.format(dateFormatter);
                series.getData().add(new XYChart.Data<>(fechaFormateada, total));
            });

            // Configurar el gráfico
            graficoBarras.getData().add(series);
            graficoBarras.setAnimated(false);

            // Ajustar espaciado basado en la variación
            graficoBarras.setCategoryGap((int) (15 + (variacionLocal * 5))); // Entre 15 y 20
            graficoBarras.setBarGap((int) (5 + (variacionLocal * 3))); // Entre 5 y 8

            // Configurar los ejes para mejor legibilidad
            CategoryAxis xAxis = (CategoryAxis) graficoBarras.getXAxis();
            NumberAxis yAxis = (NumberAxis) graficoBarras.getYAxis();
            
            // Formato adaptativo para el eje Y
            yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
                @Override
                public String toString(Number number) {
                    double valor = number.doubleValue();
                    if (valor >= 1000000) {
                        return String.format("%.1fM", valor / 1000000);
                    } else if (valor >= 1000) {
                        return String.format("%.1fK", valor / 1000);
                    } else {
                        return currencyFormat.format(valor);
                    }
                }
            });
            
            // Ajustar el número de ticks basado en el rango
            if (maxValorLocal >= 1000000) {
                yAxis.setTickUnit(200000);
            } else if (maxValorLocal >= 100000) {
                yAxis.setTickUnit(50000);
            } else if (maxValorLocal >= 10000) {
                yAxis.setTickUnit(5000);
            } else {
                yAxis.setTickUnit(1000);
            }
            
            // Configurar el eje X
            xAxis.setTickLabelRotation(45);
            xAxis.setTickLabelGap(10);
            xAxis.setTickLabelFont(new Font("Arial", 10));
            
            // Configurar el eje Y
            yAxis.setTickLabelFont(new Font("Arial", 10));
            yAxis.setTickLength(10);
            yAxis.setMinorTickVisible(true);
            yAxis.setMinorTickLength(10);
            
            // Configurar el gráfico para que se ajuste al contenedor
            graficoBarras.setMinWidth(800);
            graficoBarras.setMinHeight(400);
            graficoBarras.setPrefSize(1000, 500);
            graficoBarras.setMaxSize(1200, 800);
            graficoBarras.setPadding(new Insets(40, 40, 40, 40));
            
            // Ajustar el espaciado entre elementos
            graficoBarras.setCategoryGap(20);
            graficoBarras.setBarGap(10);
            
            // Ajustar el título
            graficoBarras.setTitle("Ventas por Día");
            graficoBarras.setLegendVisible(false);

            // Crear variables locales para uso en lambda
            final double maxValorFinal = maxValorLocal;

            // Aplicar estilos a las barras
            for (XYChart.Series<String, Number> s : graficoBarras.getData()) {
                for (XYChart.Data<String, Number> data : s.getData()) {
                    data.nodeProperty().addListener((ov, oldNode, newNode) -> {
                        if (newNode != null) {
                            // Estilo de las barras con color basado en el valor
                            double barValor = data.getYValue().doubleValue();
                            double porcentajeMax = barValor / maxValorFinal;
                            String color = String.format("#%02x%02x%02x", 
                                (int) (36 + (212 * porcentajeMax)),
                                (int) (152 + (106 * porcentajeMax)),
                                (int) (219 + (36 * porcentajeMax)));
                            
                            newNode.setStyle("-fx-bar-fill: " + color + "; -fx-cursor: hand; " +
                                           "-fx-bar-fill-gradient-stops: 0.0, 0.5; " +
                                           "-fx-bar-fill-gradient-end-color: " + 
                                           String.format("#%02x%02x%02x", 
                                               (int) (36 + (106 * porcentajeMax)),
                                               (int) (152 + (53 * porcentajeMax)),
                                               (int) (219 + (18 * porcentajeMax))));
                            
                            // Mostrar tooltip con más detalles
                            String tooltipText = String.format(
                                    """
                                            Fecha: %s
                                            Ventas: %s
                                            Valor: %s""",
                                data.getXValue(),
                                numberFormat.format(barValor),
                                currencyFormat.format(barValor)
                            );
                            
                            Tooltip tooltip = new Tooltip(tooltipText);
                            tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: " + color + "; -fx-text-fill: white;");
                            Tooltip.install(newNode, tooltip);
                        }
                    });
                }
            }
            
        } catch (Exception e) {
            mostrarError("Error al actualizar el gráfico de barras: " + e.getMessage());
        }
    }

    private void actualizarGraficoPastel(List<Factura> facturas) {
        try {
            graficoPastel.getData().clear();
            
            if (facturas == null || facturas.isEmpty()) {
                // Mostrar mensaje cuando no hay datos
                graficoPastel.setTitle("No hay datos de ventas para mostrar");
                return;
            }
            
            // Configuración inicial del gráfico
            graficoPastel.setTitle("Distribución de Ventas");
            graficoPastel.setLabelsVisible(true);
            graficoPastel.setLegendVisible(true);
            graficoPastel.setAnimated(false);
            graficoPastel.setClockwise(true);
            graficoPastel.setLabelLineLength(10);
            graficoPastel.setStartAngle(90); // Comenzar desde arriba

            // Crear un mapa para contar productos vendidos
            Map<String, Double> ventasPorProducto = new HashMap<>();
            
            // Contar ventas por producto
            for (Factura factura : facturas) {
                if (factura != null && factura.getProductos() != null) {
                    for (Productos producto : factura.getProductos()) {
                        if (producto != null && producto.getNombre() != null) {
                            String nombreProducto = producto.getNombre();
                            double valor = producto.getPrecio() * (producto.getCantidad() > 0 ? producto.getCantidad() : 1);
                            ventasPorProducto.merge(nombreProducto, valor, Double::sum);
                        }
                    }
                }
            }
            
            // Ordenar por valor descendente y tomar los 5 primeros
            List<Map.Entry<String, Double>> topProductos = ventasPorProducto.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .toList();
            
            // Crear datos para el gráfico
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            // Colores para el gráfico (colores más vivos y contrastantes)
            String[] pieColors = {
                "#3498db", "#2ecc71", "#e74c3c", "#f1c40f", "#9b59b6",
                "#1abc9c", "#e67e22", "#34495e", "#d35400", "#16a085"
            };
            int colorIndex = 0;
            
            // Calcular total para porcentajes
            double totalVentas = topProductos.stream()
                    .mapToDouble(Map.Entry::getValue)
                    .sum();
            
            // Agregar datos al gráfico
            for (Map.Entry<String, Double> entry : topProductos) {
                double porcentaje = totalVentas > 0 ? (entry.getValue() / totalVentas) * 100 : 0;
                String etiqueta = String.format("%s (%.1f%%)", entry.getKey(), porcentaje);
                
                PieChart.Data data = new PieChart.Data(etiqueta, entry.getValue());
                pieChartData.add(data);
                
                // Asignar color al segmento
                if (colorIndex < pieColors.length) {
                    final String color = pieColors[colorIndex];
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            newNode.setStyle("-fx-pie-color: " + color + ";");
                            
                            // Agregar tooltip
                            Tooltip tooltip = new Tooltip(String.format("%s\n%s", 
                                entry.getKey(), 
                                currencyFormat.format(entry.getValue())));
                            Tooltip.install(newNode, tooltip);
                        }
                    });
                }
                colorIndex++;
            }
            
            // Configurar el gráfico
            graficoPastel.setData(pieChartData);
            
            // Asegurar que el gráfico sea visible
            graficoPastel.setMinSize(400, 300);
            graficoPastel.setPrefSize(600, 400);
            graficoPastel.setMaxSize(800, 600);
            
            // Aplicar estilos CSS
            graficoPastel.setStyle(
                "-fx-padding: 20;" +
                "-fx-font-size: 12px;" +
                "-fx-legend-visible: true;" +
                "-fx-legend-side: right;" +
                "-fx-pie-label-visible: true;" +
                "-fx-label-line-length: 10;" +
                "-fx-start-angle: 90;"
            );
            
            // Mostrar mensaje si no hay datos
            if (pieChartData.isEmpty()) {
                graficoPastel.setTitle("No hay suficientes datos para mostrar el gráfico");
            }
            
        } catch (Exception e) {
            mostrarError("Error al actualizar el gráfico de pastel: " + e.getMessage());
        }
    }

    private void actualizarTablaProductos() {
        try {
            if (productos == null) {
                return;
            }
            
            // Filtrar productos con existencias y ordenar por cantidad descendente
            List<Productos> productosFiltrados = productos.stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getCantidad() > 0)
                    .sorted((p1, p2) -> Integer.compare(p2.getCantidad(), p1.getCantidad()))
                    .collect(Collectors.toList());
            
            // Actualizar tabla
            ObservableList<Productos> items = FXCollections.observableArrayList(productosFiltrados);
            tablaProductos.setItems(items);
            tablaProductos.sort();
            
        } catch (Exception e) {
            mostrarError("Error al actualizar la tabla de productos: " + e.getMessage());
        }
    }

    @FXML
    private void onVolverButtonClick() {
        try {
            if (contenedorGraficos != null && contenedorGraficos.getScene() != null && contenedorGraficos.getScene().getWindow() != null) {
                ((javafx.stage.Stage) contenedorGraficos.getScene().getWindow()).close();
            }
        } catch (Exception e) {
            mostrarError("Error al cerrar la ventana: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error al mostrar mensaje de error: " + e.getMessage());
        }
    }

    private void mostrarInfo(String mensaje) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Búsqueda completada");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error al mostrar mensaje informativo: " + e.getMessage());
        }
    }
}
