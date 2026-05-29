package LogicaTienda.Forms;

import LogicaTienda.Model.Productos;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchForm extends Stage {

    private List<Productos> listaOriginal;
    @Setter
    private Consumer<List<Productos>> onBusquedaFinalizada;

    private TextField txtBusquedaId = new TextField();
    private TextField txtBusquedaNombre = new TextField();
    private TableView<Productos> tablaResultados = new TableView<>();

    public SearchForm(List<Productos> productos) {
        this.listaOriginal = productos;

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Buscar Productos");

        // Configurar los TextFields
        txtBusquedaId.setPromptText("Buscar por ID");
        txtBusquedaNombre.setPromptText("Buscar por Nombre");

        // Configurar tabla resultados (solo columnas relevantes)
        TableColumn<Productos, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> {
            Productos producto = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(producto != null ? producto.getIdProducto() : "");
        });

        TableColumn<Productos, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cellData -> {
            Productos producto = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(producto != null ? producto.getNombre() : "");
        });

        tablaResultados.getColumns().addAll(colId, colNombre);

        // Cargar datos iniciales
        tablaResultados.setItems(FXCollections.observableArrayList(listaOriginal));

        // Evento búsqueda en tiempo real en ambos campos
        txtBusquedaId.textProperty().addListener((obs, oldVal, newVal) -> filtrarTabla());
        txtBusquedaNombre.textProperty().addListener((obs, oldVal, newVal) -> filtrarTabla());

        // Botón para confirmar selección o cerrar
        Button btnAceptar = new Button("Aceptar");
        btnAceptar.setOnAction(e -> {
            if (onBusquedaFinalizada != null) {
                // Devuelve la lista filtrada que está en la tabla
                onBusquedaFinalizada.accept(tablaResultados.getItems());
            }
            close();
        });

        HBox filtros = new HBox(10, txtBusquedaId, txtBusquedaNombre);
        VBox layout = new VBox(10, filtros, tablaResultados, btnAceptar);
        layout.setPrefSize(400, 400);
        Scene scene = new Scene(layout);
        setScene(scene);
    }

    private void filtrarTabla() {
        String filtroId = txtBusquedaId.getText();
        String filtroNombre = txtBusquedaNombre.getText();

        List<Productos> filtrados = listaOriginal.stream()
                .filter(p -> (filtroId == null || filtroId.isBlank() || p.getIdProducto().toLowerCase().contains(filtroId.toLowerCase())) &&
                        (filtroNombre == null || filtroNombre.isBlank() || p.getNombre().toLowerCase().contains(filtroNombre.toLowerCase())))
                .collect(Collectors.toList());

        tablaResultados.setItems(FXCollections.observableArrayList(filtrados));
    }
}
