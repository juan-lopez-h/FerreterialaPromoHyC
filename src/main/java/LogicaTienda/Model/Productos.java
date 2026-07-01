package LogicaTienda.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class Productos implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String idProducto;
    private String nombre;
    private double precio;
    private double precioParaVender;
    private double porcentajeGanancia;
    private int cantidad;

    // No-arg constructor
    public Productos() {
        this.idProducto = "";
        this.nombre = "";
        this.precio = 0.0;
        this.precioParaVender = 0.0;
        this.porcentajeGanancia = 0.0;
        this.cantidad = 0;
    }

    public Productos(String idProducto, String nombre, double precio, int cantidad, int stock) {
        this();
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.porcentajeGanancia = 0;
        this.precioParaVender = precio;
    }

    public Productos(String idProducto, String nombre, double precio, double porcentajeGanancia, int cantidad, int stock) {
        this();
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.porcentajeGanancia = porcentajeGanancia;
        this.cantidad = cantidad;
        calcularPrecioVenta();
    }

    public void calcularPrecioVenta() {
        double precioConGanancia = this.precio * (1 + (this.porcentajeGanancia / 100));
        this.precioParaVender = Math.round(precioConGanancia);
    }

    @Override
    public String toString() {
        return "nombre del producto: " + this.nombre + " ID del producto: " + this.idProducto + " cantidad en almacen: " + this.cantidad + " el precio de costo: " + this.precio + " el precio de venta: " + this.precioParaVender;
    }
}
