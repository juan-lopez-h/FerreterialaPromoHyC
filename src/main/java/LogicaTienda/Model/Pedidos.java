package LogicaTienda.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase que representa un pedido de productos
 */
@Getter
@Setter
public class Pedidos implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String idPedido;
    private String numeroPedido;
    private Date fechaPedido;
    private Date fechaEntrega;
    private String idCliente;
    private String nombreCliente;
    private String telefonoCliente;
    private String idDomicilio;
    private List<ItemPedido> items;
    private double subtotal;
    private double costoEnvio;
    private double total;
    private String estado; // Pendiente, En Proceso, Enviado, Entregado, Cancelado
    private String observaciones;
    private String metodoPago;
    private boolean pagado;
    private String idFactura; // Referencia a la factura si está facturado

    // No-arg constructor
    public Pedidos() {
        this.idPedido = "";
        this.numeroPedido = "";
        this.fechaPedido = new Date();
        this.fechaEntrega = null;
        this.idCliente = "";
        this.nombreCliente = "";
        this.telefonoCliente = "";
        this.idDomicilio = "";
        this.items = new ArrayList<>();
        this.subtotal = 0.0;
        this.costoEnvio = 0.0;
        this.total = 0.0;
        this.estado = "Pendiente";
        this.observaciones = "";
        this.metodoPago = "Efectivo";
        this.pagado = false;
        this.idFactura = "";
    }

    public Pedidos(String idPedido, String numeroPedido, String idCliente, String nombreCliente) {
        this();
        this.idPedido = idPedido;
        this.numeroPedido = numeroPedido;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
    }

    /**
     * Agrega un item al pedido y recalcula los totales
     * @param item Item a agregar
     */
    public void agregarItem(ItemPedido item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        calcularTotales();
    }

    /**
     * Calcula los totales del pedido
     */
    public void calcularTotales() {
        this.subtotal = 0.0;
        if (items != null) {
            for (ItemPedido item : items) {
                this.subtotal += item.getSubtotal();
            }
        }
        this.total = this.subtotal + this.costoEnvio;
    }

    public String toString() {
        return "Pedido: " + this.numeroPedido + " - Cliente: " + this.nombreCliente +
               " - Fecha: " + this.fechaPedido + " - Estado: " + this.estado;
    }

    /**
     * Clase interna que representa un item del pedido
     */
    @Getter
    @Setter
    public static class ItemPedido implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String idProducto;
        private String nombreProducto;
        private int cantidad;
        private double precioUnitario;
        private double subtotal;

        public ItemPedido() {
            this.idProducto = "";
            this.nombreProducto = "";
            this.cantidad = 0;
            this.precioUnitario = 0.0;
            this.subtotal = 0.0;
        }

        public ItemPedido(String idProducto, String nombreProducto, int cantidad, double precioUnitario) {
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