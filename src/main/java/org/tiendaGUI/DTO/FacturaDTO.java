package org.tiendaGUI.DTO;

public class FacturaDTO {

    private String idFactura;
    private double montoTotal;
    private String fechaEmision;

    public FacturaDTO(String idFactura, double montoTotal, String fechaEmision) {
        this.idFactura = idFactura;
        this.montoTotal = montoTotal;
        this.fechaEmision = fechaEmision;
    }

    public String getIdFactura() {
        return idFactura;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }
}
