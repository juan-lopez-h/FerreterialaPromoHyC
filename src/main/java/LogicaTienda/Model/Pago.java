package LogicaTienda.Model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Pago implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private Double monto;
    private String metodoPago;
    private String fecha;
    private String estado;
    private String referencia;

    // Constructor
    public Pago(String id, Double monto, String metodoPago, String fecha, String estado, String referencia) {
        this.id = id;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.estado = estado;
        this.referencia = referencia;
    }
}