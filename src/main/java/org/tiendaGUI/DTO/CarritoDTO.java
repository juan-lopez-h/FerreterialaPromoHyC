package org.tiendaGUI.DTO;

import LogicaTienda.Model.Productos;
import java.util.List;
import java.util.Objects;

public class CarritoDTO {

    private List<Productos> productos;
    private double total;

    public CarritoDTO(List<Productos> productos, double total) {
        this.productos = productos;
        this.total = total;
    }

    public List<Productos> getProductos() {
        return productos;
    }

    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    public void actualizarTotal() {
        if (productos == null || productos.isEmpty()) {
            this.total = 0.0;
            return;
        }
        this.total = productos.stream()
                .filter(Objects::nonNull)
                .mapToDouble(p -> p.getPrecioParaVender() * p.getCantidad())
                .sum();
    }
}
