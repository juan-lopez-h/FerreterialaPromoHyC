package org.tiendaGUI.DTO;

public class DomicilioDTO {

    private String direccion;
    private String referenciaDireccion;
    private String numeroPostal;
    private String numeroApartamento;
    private String numeroCelular;
    private String fechaEntrega;
    private String idFactura;

    public DomicilioDTO(String direccion, String referenciaDireccion, String numeroPostal,
                        String numeroApartamento, String numeroCelular, String fechaEntrega,
                        String idFactura) {
        this.direccion = direccion;
        this.referenciaDireccion = referenciaDireccion;
        this.numeroPostal = numeroPostal;
        this.numeroApartamento = numeroApartamento;
        this.numeroCelular = numeroCelular;
        this.fechaEntrega = fechaEntrega;
        this.idFactura = idFactura;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getReferenciaDireccion() {
        return referenciaDireccion;
    }

    public String getNumeroPostal() {
        return numeroPostal;
    }

    public String getNumeroApartamento() {
        return numeroApartamento;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public String getIdFactura() {
        return idFactura;
    }
}
