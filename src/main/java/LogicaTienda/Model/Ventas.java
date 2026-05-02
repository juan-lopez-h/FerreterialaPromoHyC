package LogicaTienda.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase que representa una venta realizada
 */
@Getter
@Setter
public class Ventas implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String idVenta;
    private String numeroVenta;
    private Date fechaVenta;
    private String idCliente;
    private String nombreCliente;
    private String idVendedor;
    private String nombreVendedor;
    private List<ItemVenta> items;
    private double subtotal;
    private double impuestos;
    private double descuento;
    private double total;
    private String formaPago;
    private boolean facturada;
    private String idFactura;
    private String estado; // Completada, Anulada, Devuelta
    private String observaciones;

    // No-arg constructor
    public Ventas() {
        this.idVenta = "";
        this.numeroVenta = "";
        this.fechaVenta = new Date();
        this.idCliente = "";
        this.nombreCliente = "";
        this.idVendedor = "";
        this.nombreVendedor = "";
        this.items = new ArrayList<>();
        this.subtotal = 0.0;
        this.impuestos = 0.0;
        this.descuento = 0.0;
        this.total = 0.0;
        this.formaPago = "Efectivo";
        this.facturada = false;
        this.idFactura = "";
        this.estado = "Completada";
        this.observaciones = "";
    }

    public Ventas(String idVenta, String numeroVenta, String idCliente, String nombreCliente, String idVendedor, String nombreVendedor) {
        this();
        this.idVenta = idVenta;
        this.numeroVenta = numeroVenta;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.idVendedor = idVendedor;
        this.nombreVendedor = nombreVendedor;
    }

    /**
     * Agrega un item a la venta y recalcula los totales
     * @param item Item a agregar
     */
    public void agregarItem(ItemVenta item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        calcularTotales();
    }

    /**
     * Elimina un item de la venta
     * @param idProducto ID del producto a eliminar
     */
    public void eliminarItem(String idProducto) {
        if (items != null) {
            items.removeIf(item -> item.getIdProducto() != null && item.getIdProducto().equals(idProducto));
            calcularTotales();
        }
    }

    /**
     * Calcula los totales de la venta
     */
    public void calcularTotales() {
        this.subtotal = 0.0;
        if (items != null) {
            for (ItemVenta item : items) {
                this.subtotal += item.getSubtotal();
            }
        }
        // Calcular impuestos (19% IVA)
        this.impuestos = this.subtotal * 0.19;
        this.total = this.subtotal + this.impuestos - this.descuento;
    }

    /**
     * Aplica un descuento a la venta
     * @param descuento Monto del descuento
     */
    public void aplicarDescuento(double descuento) {
        this.descuento = descuento;
        calcularTotales();
    }

    /**
     * Marca la venta como facturada
     * @param idFactura ID de la factura generada
     */
    public void marcarComoFacturada(String idFactura) {
        this.facturada = true;
        this.idFactura = idFactura;
    }

    /**
     * Anula la venta
     * @param observacion Motivo de la anulación
     */
    public void anular(String observacion) {
        this.estado = "Anulada";
        this.observaciones = observacion;
    }

    public String toString() {
        return "Venta: " + this.numeroVenta + " - Cliente: " + this.nombreCliente +
               " - Fecha: " + this.fechaVenta + " - Total: " + this.total;
    }

    /**
     * Clase interna que representa un item de la venta
     */
    @Getter
    @Setter
    public static class ItemVenta implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String idProducto;
        private String nombreProducto;
        private int cantidad;
        private double precioUnitario;
        private double subtotal;

        public ItemVenta() {
            this.idProducto = "";
            this.nombreProducto = "";
            this.cantidad = 0;
            this.precioUnitario = 0.0;
            this.subtotal = 0.0;
        }

        public ItemVenta(String idProducto, String nombreProducto, int cantidad, double precioUnitario) {
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