package LogicaTienda.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase que representa un carrito de compras
 */
@Getter
@Setter
public class Carrito implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String idCarrito;
    private String idCliente;
    private String nombreCliente;
    private Date fechaCreacion;
    private Date fechaActualizacion;
    private List<ItemCarrito> items;
    private double subtotal;
    private double impuestos;
    private double total;
    private boolean activo;
    private String estado; // Activo, Abandonado, Convertido a Pedido

    // No-arg constructor
    public Carrito() {
        this.idCarrito = "";
        this.idCliente = "";
        this.nombreCliente = "";
        this.fechaCreacion = new Date();
        this.fechaActualizacion = new Date();
        this.items = new ArrayList<>();
        this.subtotal = 0.0;
        this.impuestos = 0.0;
        this.total = 0.0;
        this.activo = true;
        this.estado = "Activo";
    }

    public Carrito(String idCarrito, String idCliente, String nombreCliente) {
        this();
        this.idCarrito = idCarrito;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
    }

    /**
     * Agrega un item al carrito y recalcula los totales
     * @param item Item a agregar
     */
    public void agregarItem(ItemCarrito item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        // Verificar si el producto ya existe en el carrito
        boolean encontrado = false;
        for (ItemCarrito existente : items) {
            if (existente.getIdProducto() != null && existente.getIdProducto().equals(item.getIdProducto())) {
                // Actualizar cantidad
                existente.setCantidad(existente.getCantidad() + item.getCantidad());
                existente.calcularSubtotal();
                encontrado = true;
                break;
            }
        }

        // Si no existe, agregar el nuevo item
        if (!encontrado) {
            items.add(item);
        }

        this.fechaActualizacion = new Date();
        calcularTotales();
    }

    /**
     * Elimina un item del carrito
     * @param idProducto ID del producto a eliminar
     */
    public void eliminarItem(String idProducto) {
        if (items != null) {
            items.removeIf(item -> item.getIdProducto() != null && item.getIdProducto().equals(idProducto));
            this.fechaActualizacion = new Date();
            calcularTotales();
        }
    }

    /**
     * Actualiza la cantidad de un item en el carrito
     * @param idProducto ID del producto a actualizar
     * @param cantidad Nueva cantidad
     */
    public void actualizarCantidad(String idProducto, int cantidad) {
        if (items != null) {
            for (ItemCarrito item : items) {
                if (item.getIdProducto() != null && item.getIdProducto().equals(idProducto)) {
                    item.setCantidad(cantidad);
                    item.calcularSubtotal();
                    this.fechaActualizacion = new Date();
                    calcularTotales();
                    break;
                }
            }
        }
    }

    /**
     * Vacía el carrito
     */
    public void vaciar() {
        if (items != null) {
            items.clear();
            this.fechaActualizacion = new Date();
            calcularTotales();
        }
    }

    /**
     * Calcula los totales del carrito
     */
    public void calcularTotales() {
        this.subtotal = 0.0;
        if (items != null) {
            for (ItemCarrito item : items) {
                this.subtotal += item.getSubtotal();
            }
        }
        // Calcular impuestos (19% IVA)
        this.impuestos = this.subtotal * 0.19;
        this.total = this.subtotal + this.impuestos;
    }

    public String toString() {
        return "Carrito: " + this.idCarrito + " - Cliente: " + this.nombreCliente +
               " - Items: " + (items != null ? items.size() : 0) + " - Total: " + this.total;
    }

    /**
     * Clase interna que representa un item del carrito
     */
    @Getter
    @Setter
    public static class ItemCarrito implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String idProducto;
        private String nombreProducto;
        private int cantidad;
        private double precioUnitario;
        private double subtotal;

        public ItemCarrito() {
            this.idProducto = "";
            this.nombreProducto = "";
            this.cantidad = 0;
            this.precioUnitario = 0.0;
            this.subtotal = 0.0;
        }

        public ItemCarrito(String idProducto, String nombreProducto, int cantidad, double precioUnitario) {
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            calcularSubtotal();
        }

        public void calcularSubtotal() {
            this.subtotal = this.cantidad * this.precioUnitario;
        }
    }
}