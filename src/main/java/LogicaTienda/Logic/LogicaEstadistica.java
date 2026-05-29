package LogicaTienda.Logic;

import LogicaTienda.Model.Factura;
import LogicaTienda.Model.Productos;
import LogicaTienda.Services.FacturaService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LogicaEstadistica {
    private List<Productos> listaProductos;

    public LogicaEstadistica(List<Productos> listaProductos) {
        this.listaProductos = (listaProductos != null) ? listaProductos : new ArrayList<>();
    }

    private static final Logger LOGGER = Logger.getLogger(LogicaEstadistica.class.getName());
    
    // Calcular ventas por día
    public Map<LocalDate, Double> calcularVentasPorDia() {
        try {
            List<Factura> facturas = FacturaService.obtenerFacturasActivas()
                    .stream()
                    .filter(f -> !"Anulada".equals(f.getEstado()))
                    .collect(Collectors.toList());

            // Agrupar facturas por día y sumar los totales
            return facturas.stream()
                    .collect(Collectors.groupingBy(
                            factura -> factura.getFecha().toLocalDate(),
                            Collectors.summingDouble(Factura::getTotal)
                    ));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al calcular ventas por día", e);
            return new HashMap<>();
        }
    }

    // Calcular el promedio de precios de los productos
    public double calcularPromedioPrecios() {
        return listaProductos.isEmpty() ? 0.0 :
                listaProductos.stream().mapToDouble(Productos::getPrecio).average().orElse(0.0);
    }

    // Calcular la suma total de todos los precios de los productos
    public double calcularSumaTotalPrecios() {
        return listaProductos.stream().mapToDouble(Productos::getPrecio).sum();
    }

    // Calcular la cantidad total de productos en stock
    public int calcularTotalProductosEnStock() {
        return listaProductos.stream().mapToInt(p -> p.getStock() + p.getCantidad()).sum();
    }
    
    public List<Productos> getListaProductos() {
        return listaProductos;
    }

    // Calcular el precio más alto entre los productos
    public double calcularPrecioMasAlto() {
        return listaProductos.stream()
                .mapToDouble(Productos::getPrecio)
                .max()
                .orElse(0.0);
    }

    // Calcular el precio más bajo entre los productos
    public double calcularPrecioMasBajo() {
        return listaProductos.stream()
                .mapToDouble(Productos::getPrecio)
                .min()
                .orElse(0.0);
    }

    // Obtener el producto más caro
    public Productos obtenerProductoMasCaro() {
        return listaProductos.stream()
                .max(Comparator.comparingDouble(Productos::getPrecio))
                .orElse(null);
    }

    // Obtener el producto con más stock
    public Productos obtenerProductoConMasStock() {
        return listaProductos.stream()
                .max(Comparator.comparingInt(Productos::getStock))
                .orElse(null);
    }

    // Calcular el total de ventas sumando todas las facturas (excluyendo anuladas)
    public double calcularTotalVentas() {
        try {
            return FacturaService.obtenerFacturasActivas().stream()
                    .filter(f -> !"Anulada".equals(f.getEstado()))
                    .mapToDouble(Factura::getTotal)
                    .sum();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al calcular el total de ventas", e);
            return 0.0;
        }
    }

    // Calcular el valor total del inventario (precio de venta * stock para todos los productos)
    public double calcularValorTotalInventario() {
        return listaProductos.stream()
                .mapToDouble(producto -> producto.getPrecioParaVender() * (producto.getStock() + producto.getCantidad()))
                .sum();
    }
}
