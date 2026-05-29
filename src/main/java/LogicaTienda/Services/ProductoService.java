package LogicaTienda.Services;

import LogicaTienda.Data.H2Database;
import LogicaTienda.Model.Productos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductoService {
    private ProductoService() {
    }

    public static List<Productos> obtenerTodosLosProductos() {
        List<Productos> productos = new ArrayList<>();
        String sql = "SELECT id_producto, nombre, precio, precio_para_vender, porcentaje_ganancia, cantidad, stock FROM productos ORDER BY nombre";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener productos: " + e.getMessage());
        }

        return productos;
    }

    public static Productos buscarProductoPorId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        String sql = "SELECT id_producto, nombre, precio, precio_para_vender, porcentaje_ganancia, cantidad, stock FROM productos WHERE id_producto = ?";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProducto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar producto por ID: " + e.getMessage());
        }

        return null;
    }

    public static void guardarProducto(Productos producto) {
        if (producto == null) {
            return;
        }

        if (producto.getIdProducto() == null || producto.getIdProducto().isBlank()) {
            producto.setIdProducto(generarIdProductoUnico());
        }

        if (buscarProductoPorId(producto.getIdProducto()) != null) {
            actualizarProducto(producto);
            return;
        }

        if (producto.getPrecioParaVender() <= 0 && producto.getPorcentajeGanancia() > 0) {
            producto.calcularPrecioVenta();
        }

        String sql = "INSERT INTO productos (id_producto, nombre, precio, precio_para_vender, porcentaje_ganancia, cantidad, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getIdProducto());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            ps.setDouble(4, producto.getPrecioParaVender());
            ps.setDouble(5, producto.getPorcentajeGanancia());
            ps.setInt(6, producto.getCantidad());
            ps.setInt(7, producto.getStock());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ No se pudo guardar el producto: " + e.getMessage());
        }
    }

    public static void actualizarProducto(Productos producto) {
        if (producto == null || producto.getIdProducto() == null || producto.getIdProducto().isBlank()) {
            return;
        }

        if (producto.getPrecioParaVender() <= 0 && producto.getPorcentajeGanancia() > 0) {
            producto.calcularPrecioVenta();
        }

        String sql = "UPDATE productos SET nombre = ?, precio = ?, precio_para_vender = ?, porcentaje_ganancia = ?, cantidad = ?, stock = ? WHERE id_producto = ?";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setDouble(3, producto.getPrecioParaVender());
            ps.setDouble(4, producto.getPorcentajeGanancia());
            ps.setInt(5, producto.getCantidad());
            ps.setInt(6, producto.getStock());
            ps.setString(7, producto.getIdProducto());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ No se pudo actualizar el producto: " + e.getMessage());
        }
    }

    public static void eliminarProducto(String idProducto) {
        if (idProducto == null || idProducto.isBlank()) {
            return;
        }

        String sql = "DELETE FROM productos WHERE id_producto = ?";
        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idProducto);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ No se pudo eliminar el producto: " + e.getMessage());
        }
    }

    public static void actualizarStock(String idProducto, int cantidad, boolean esVenta) {
        if (idProducto == null || idProducto.isBlank()) {
            System.err.println("Error: ID de producto no válido");
            return;
        }

        try (Connection conn = H2Database.getConnection()) {
            ajustarStock(conn, idProducto, cantidad, esVenta);
        } catch (SQLException e) {
            System.err.println("Error al actualizar el stock del producto " + idProducto + ": " + e.getMessage());
        }
    }

    static void ajustarStock(Connection conn, String idProducto, int cantidad, boolean esVenta) throws SQLException {
        if (idProducto == null || idProducto.isBlank()) {
            return;
        }

        String buscarSql = "SELECT cantidad, stock FROM productos WHERE id_producto = ?";
        try (PreparedStatement buscar = conn.prepareStatement(buscarSql)) {
            buscar.setString(1, idProducto);
            try (ResultSet rs = buscar.executeQuery()) {
                if (!rs.next()) {
                    System.err.println("Producto no encontrado para actualizar stock: " + idProducto);
                    return;
                }

                int cantidadActual = rs.getInt("cantidad");
                int stockActual = rs.getInt("stock");

                if (esVenta) {
                    int cantidadARestar = Math.min(cantidad, cantidadActual);
                    int stockARestar = Math.max(0, cantidad - cantidadARestar);

                    String updateSql = "UPDATE productos SET cantidad = GREATEST(cantidad - ?, 0), stock = GREATEST(stock - ?, 0) WHERE id_producto = ?";
                    try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                        update.setInt(1, cantidadARestar);
                        update.setInt(2, stockARestar);
                        update.setString(3, idProducto);
                        update.executeUpdate();
                    }
                } else {
                    String updateSql = "UPDATE productos SET cantidad = cantidad + ? WHERE id_producto = ?";
                    try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                        update.setInt(1, cantidad);
                        update.setString(2, idProducto);
                        update.executeUpdate();
                    }
                }

                System.out.println("Stock actualizado para el producto " + idProducto +
                        ". Operación: " + (esVenta ? "Venta" : "Devolución") +
                        ", Cantidad: " + cantidad +
                        ", Stock previo: " + stockActual +
                        ", Cantidad previa: " + cantidadActual);
            }
        }
    }

    private static Productos mapProducto(ResultSet rs) throws SQLException {
        Productos producto = new Productos();
        producto.setIdProducto(rs.getString("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setPrecio(rs.getDouble("precio"));
        producto.setPrecioParaVender(rs.getDouble("precio_para_vender"));
        producto.setPorcentajeGanancia(rs.getDouble("porcentaje_ganancia"));
        producto.setCantidad(rs.getInt("cantidad"));
        producto.setStock(rs.getInt("stock"));
        return producto;
    }

    private static String generarIdProductoUnico() {
        String id;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (buscarProductoPorId(id) != null);
        return id;
    }
}
