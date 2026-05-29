package LogicaTienda.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Domicilio implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String direccion;
    private String referenciaDireccion;
    private String numeroPostal;
    private String numeroApartamento;
    private String numeroCelular;
    private LocalDate fechaEntrega;
    private String idFactura;
    private String clienteIdentificacion;
    private String estadoDomicilio;

    public Domicilio(String direccion, String referenciaDireccion, String numeroPostal,
                    String numeroApartamento, String numeroCelular, LocalDate fechaEntrega,
                    String idFactura, String clienteIdentificacion) {
        this.direccion = direccion;
        this.referenciaDireccion = referenciaDireccion;
        this.numeroPostal = numeroPostal;
        this.numeroApartamento = numeroApartamento;
        this.numeroCelular = numeroCelular;
        this.fechaEntrega = fechaEntrega;
        this.idFactura = idFactura;
        this.clienteIdentificacion = clienteIdentificacion;
        this.estadoDomicilio = "Pendiente";
    }
}