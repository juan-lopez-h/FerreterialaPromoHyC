package org.tiendaGUI.DTO;

import lombok.Getter;

@Getter
public class ProductoSimpleDTO {

    private String idProducto;
    private String nombre;
    private double precio;
    private int cantidad;
    private int stock;

    public ProductoSimpleDTO(String idProducto,
                             String nombre,
                             double precio,
                             int cantidad,
                             int stock) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.stock = stock;
    }
}
