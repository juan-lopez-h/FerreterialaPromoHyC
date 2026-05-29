package LogicaTienda.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Pedido {
    private String idPedido;
    private int cantidad;
    private String estadoPedido;
    private double precioSubTotal;
    private final ObservableList<Productos> productosSubTotal = FXCollections.observableArrayList();
    @Override
    public String toString() {
    return "Pedido{" + "cantidad=" + cantidad + '}' + "productosSubTotal=" + precioSubTotal + '}';
    }
}
