package LogicaTienda.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Clase que representa un domicilio para entregas
 */
@Getter
@Setter
public class Domicilios implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String idDomicilio;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String codigoPostal;
    private String telefono;
    private String nombreContacto;
    private String instruccionesEspeciales;
    private Date fechaRegistro;
    private boolean activo;

    // No-arg constructor
    public Domicilios() {
        this.idDomicilio = "";
        this.direccion = "";
        this.ciudad = "";
        this.departamento = "";
        this.codigoPostal = "";
        this.telefono = "";
        this.nombreContacto = "";
        this.instruccionesEspeciales = "";
        this.fechaRegistro = new Date();
        this.activo = true;
    }

    public Domicilios(String idDomicilio, String direccion, String ciudad, String departamento,
                     String codigoPostal, String telefono, String nombreContacto) {
        this();
        this.idDomicilio = idDomicilio;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.departamento = departamento;
        this.codigoPostal = codigoPostal;
        this.telefono = telefono;
        this.nombreContacto = nombreContacto;
    }

    public String toString() {
        return "Domicilio: " + this.direccion + ", " + this.ciudad + ", " + this.departamento +
               " - Contacto: " + this.nombreContacto + " (" + this.telefono + ")";
    }
}