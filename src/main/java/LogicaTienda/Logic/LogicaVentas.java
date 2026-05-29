package LogicaTienda.Logic;

import LogicaTienda.Model.Productos;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LogicaVentas {
    private List<Productos> listaProductos;

    public LogicaVentas() {
        this.listaProductos = new ArrayList<>(); // Se inicializa la lista
    }

    public boolean vender(Productos productoVenta, int cantidad) {
        int ubicacion = listaProductos.indexOf(productoVenta);
        if (ubicacion >= 0) {
            Productos producto = listaProductos.get(ubicacion);
            if (producto.getStock() >= cantidad) { // Verifica si hay suficiente stock
                producto.setStock(producto.getStock() - cantidad);
                return true;
            }
        }
        return false; // Venta no realizada
    }

    public boolean actualizarTodosPrecios(double porcentajeCambio) {
        if (listaProductos.isEmpty()) return false;

        for (Productos producto : listaProductos) {
            double nuevoPrecio = producto.getPrecio() * (1 + porcentajeCambio / 100);
            producto.setPrecio(nuevoPrecio);
        }
        return true;
    }

    public boolean actualizarListaProductos(List<Productos> nuevaLista) {
        if (nuevaLista == null || nuevaLista.isEmpty()) return false;

        listaProductos.clear();
        listaProductos.addAll(nuevaLista);
        return true;
    }

    public boolean limpiarListaProductos() {
        if (listaProductos.isEmpty()) return false;

        listaProductos.clear();
        return true;
    }

}
