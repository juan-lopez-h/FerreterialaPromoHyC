package LogicaTienda.Model;

import javafx.collections.FXCollections;
import lombok.Getter;
import lombok.Setter;
import javafx.collections.ObservableList;
@Getter
@Setter
public class DetallePedido {
    private String idDetallePedido;
    private static final ObservableList<Productos> Listpedido = FXCollections.observableArrayList();
    private int cantidadPedidos;
    private double precioTotal;
}
