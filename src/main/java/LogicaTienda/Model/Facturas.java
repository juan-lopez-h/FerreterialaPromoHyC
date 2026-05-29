package LogicaTienda.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase que representa una factura de venta
 */
@Getter
@Setter
public class Facturas implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String idFactura;
    private String numeroFactura;
    private Date fechaEmision;
    private String idCliente;
    private String nombreCliente;
    private String identificacionCliente;
    private String direccionCliente;
    private String telefonoCliente;
    private List<ItemFactura> items;
    private double subtotal;
    private double impuestos;
    private double total;
    private String formaPago;
    private String estado;
    private String observaciones;
    private String idVendedor;
    private String nombreVendedor;
    private boolean anulada;

    // No-arg constructor
    public Facturas() {
        this.idFactura = "";
        this.numeroFactura = "";
        this.fechaEmision = new Date();
        this.idCliente = "";
        this.nombreCliente = "";
        this.identificacionCliente = "";
        this.direccionCliente = "";
        this.telefonoCliente = "";
        this.items = new ArrayList<>();
        this.subtotal = 0.0;
        this.impuestos = 0.0;
        this.total = 0.0;
        this.formaPago = "Efectivo";
        this.estado = "Emitida";
        this.observaciones = "";
        this.idVendedor = "";
        this.nombreVendedor = "";
        this.anulada = false;
    }

    public Facturas(String idFactura, String numeroFactura, String idCliente, String nombreCliente) {
        this();
        this.idFactura = idFactura;
        this.numeroFactura = numeroFactura;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
    }

    /**
     * Agrega un item a la factura y recalcula los totales
     * @param item Item a agregar
     */
    public void agregarItem(ItemFactura item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        calcularTotales();
    }

    /**
     * Calcula los totales de la factura
     */
    public void calcularTotales() {
        this.subtotal = 0.0;
        if (items != null) {
            for (ItemFactura item : items) {
                this.subtotal += item.getSubtotal();
            }
        }
        // Calcular impuestos (19% IVA)
        this.impuestos = this.subtotal * 0.19;
        this.total = this.subtotal + this.impuestos;
    }

    public String toString() {
        return "Factura: " + this.numeroFactura + " - Cliente: " + this.nombreCliente +
               " - Fecha: " + this.fechaEmision + " - Total: " + this.total;
    }

    /**
     * Clase interna que representa un item de la factura
     */
    @Getter
    @Setter
    public static class ItemFactura implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String idProducto;
        private String nombreProducto;
        private int cantidad;
        private double precioUnitario;
        private double subtotal;

        public ItemFactura() {
            this.idProducto = "";
            this.nombreProducto = "";
            this.cantidad = 0;
            this.precioUnitario = 0.0;
            this.subtotal = 0.0;
        }

        public ItemFactura(String idProducto, String nombreProducto, int cantidad, double precioUnitario) {
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