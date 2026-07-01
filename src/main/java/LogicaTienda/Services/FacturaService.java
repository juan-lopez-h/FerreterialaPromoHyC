package LogicaTienda.Services;

import LogicaTienda.Data.H2Database;
import LogicaTienda.Model.Factura;
import LogicaTienda.Model.Pago;
import LogicaTienda.Model.Productos;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FacturaService {
    private static final Gson GSON = new Gson();

    private FacturaService() {
    }

    public static List<Factura> obtenerTodasLasFacturas() {
        return consultarFacturas("SELECT id, fecha, cliente_nombre, cliente_identificacion, tipo_documento, cliente_email, cliente_telefono, tipo_factura, estado, eliminada, total, metodo_pago, referencia_pago, pago_json FROM facturas ORDER BY fecha DESC");
    }

    public static List<Factura> obtenerFacturasActivas() {
        return consultarFacturas("SELECT id, fecha, cliente_nombre, cliente_identificacion, tipo_documento, cliente_email, cliente_telefono, tipo_factura, estado, eliminada, total, metodo_pago, referencia_pago, pago_json FROM facturas WHERE eliminada = FALSE ORDER BY fecha DESC");
    }

    public static Factura buscarFacturaPorId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        System.out.println("🔍 Buscando factura con ID: " + id);
        String sql = "SELECT id, fecha, cliente_nombre, cliente_identificacion, tipo_documento, cliente_email, cliente_telefono, tipo_factura, estado, eliminada, total, metodo_pago, referencia_pago, pago_json FROM facturas WHERE id = ?";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Factura factura = mapFactura(rs, conn);
                    System.out.println("✅ Factura encontrada en base local");
                    return factura;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar factura por ID: " + e.getMessage());
        }

        System.out.println("❌ No se encontró factura con ID: " + id);
        return null;
    }

    public static String crearFactura(List<Productos> productos, String clienteNombre, String clienteIdentificacion,
                                      double montoTotal, String metodoPago, String referenciaPago,
                                      String clienteEmail, String clienteTelefono, String tipoDocumento, String tipoFactura) {
        String facturaId = UUID.randomUUID().toString().substring(0, 8);
        List<Productos> items = productos != null ? new ArrayList<>(productos) : new ArrayList<>();
        double total = calcularTotalProductos(items);

        Factura factura = new Factura();
        factura.setId(facturaId);
        factura.setFecha(LocalDateTime.now());
        factura.setProductos(items);
        factura.setClienteNombre(clienteNombre);
        factura.setClienteIdentificacion(clienteIdentificacion);
        factura.setTipoDocumento(tipoDocumento);
        factura.setClienteEmail(clienteEmail);
        factura.setClienteTelefono(clienteTelefono);
        factura.setTotal(total > 0 ? total : montoTotal);
        factura.setMetodoPago(metodoPago);
        factura.setReferenciaPago(referenciaPago);
        factura.setTipoFactura(tipoFactura);
        factura.setEliminada(false);
        factura.setEstado("Activa");

        try (Connection conn = H2Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                insertarFactura(conn, factura);
                guardarDetalleFactura(conn, factura.getId(), items);
                for (Productos producto : items) {
                    ProductoService.ajustarStock(conn, producto.getIdProducto(), producto.getCantidad(), true);
                }
                conn.commit();
                return facturaId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("No se pudo crear la factura: " + e.getMessage());
            return null;
        }
    }

    public static void anularFactura(String idFactura) {
        if (idFactura == null || idFactura.isBlank()) {
            return;
        }

        try (Connection conn = H2Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Factura factura = buscarFacturaPorIdConConexion(conn, idFactura);
                if (factura == null) {
                    System.err.println("No hay conexión a la factura para anularla: " + idFactura);
                    return;
                }

                if (factura.isEliminada() || "Anulada".equalsIgnoreCase(factura.getEstado())) {
                    return;
                }

                actualizarEstadoFactura(conn, idFactura, true, "Anulada");
                for (Productos producto : factura.getProductos()) {
                    ProductoService.ajustarStock(conn, producto.getIdProducto(), producto.getCantidad(), false);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("No se pudo anular la factura: " + e.getMessage());
        }
    }

    public static void restaurarFactura(String idFactura) {
        if (idFactura == null || idFactura.isBlank()) {
            return;
        }

        try (Connection conn = H2Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Factura factura = buscarFacturaPorIdConConexion(conn, idFactura);
                if (factura == null) {
                    System.err.println("No hay conexión a la factura para restaurarla: " + idFactura);
                    return;
                }

                if (!factura.isEliminada() && !"Anulada".equalsIgnoreCase(factura.getEstado())) {
                    return;
                }

                actualizarEstadoFactura(conn, idFactura, false, "Activa");
                for (Productos producto : factura.getProductos()) {
                    ProductoService.ajustarStock(conn, producto.getIdProducto(), producto.getCantidad(), true);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("No se pudo restaurar la factura: " + e.getMessage());
        }
    }

    public static double calcularTotalVentas() {
        return obtenerFacturasActivas().stream()
                .filter(f -> !"Anulada".equalsIgnoreCase(f.getEstado()))
                .mapToDouble(Factura::getTotal)
                .sum();
    }

    public static void actualizarFactura(Factura factura) {
        if (factura == null || factura.getId() == null || factura.getId().isBlank()) {
            throw new IllegalArgumentException("La factura y su ID no pueden ser nulos");
        }

        try (Connection conn = H2Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Factura facturaAnterior = buscarFacturaPorIdConConexion(conn, factura.getId());
                if (facturaAnterior == null) {
                    throw new IllegalArgumentException("No existe una factura con el ID: " + factura.getId());
                }

                List<Productos> productosNuevos = factura.getProductos() != null ? new ArrayList<>(factura.getProductos()) : new ArrayList<>();
                List<Productos> productosAnteriores = facturaAnterior.getProductos() != null ? facturaAnterior.getProductos() : new ArrayList<>();

                actualizarFacturaCabecera(conn, factura);
                reemplazarDetalleFactura(conn, factura.getId(), productosNuevos);
                ajustarStockPorDiferencia(conn, productosAnteriores, productosNuevos);

                conn.commit();
                System.out.println("✅ Factura actualizada en base local - ID: " + factura.getId() + ", Fecha: " + factura.getFecha());
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("No se pudo actualizar la factura: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void actualizarFechaFactura(String facturaId, LocalDateTime nuevaFecha) {
        if (facturaId == null || facturaId.isBlank() || nuevaFecha == null) {
            throw new IllegalArgumentException("El ID de la factura y la fecha no pueden ser nulos");
        }

        System.out.println("🔍 Actualizando fecha para factura con ID: " + facturaId);
        System.out.println("🔍 Fecha NUEVA a guardar: " + nuevaFecha);

        String sql = "UPDATE facturas SET fecha = ? WHERE id = ?";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(nuevaFecha));
            ps.setString(2, facturaId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                System.err.println("❌ ERROR: No se encontró la factura con ID: " + facturaId);
                throw new IllegalArgumentException("No existe una factura con el ID: " + facturaId);
            }

            Factura facturaDespues = buscarFacturaPorId(facturaId);
            if (facturaDespues != null) {
                System.out.println("✅ Fecha actualizada en base local - ID: " + facturaId);
                System.out.println("   - Fecha nueva: " + facturaDespues.getFecha());
            }
        } catch (SQLException e) {
            System.err.println("No se pudo actualizar la fecha de la factura: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static List<Factura> consultarFacturas(String sql) {
        List<Factura> facturas = new ArrayList<>();

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                facturas.add(mapFactura(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al consultar facturas: " + e.getMessage());
        }

        return facturas;
    }

    private static Factura buscarFacturaPorIdConConexion(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, fecha, cliente_nombre, cliente_identificacion, tipo_documento, cliente_email, cliente_telefono, tipo_factura, estado, eliminada, total, metodo_pago, referencia_pago, pago_json FROM facturas WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFactura(rs, conn);
                }
            }
        }

        return null;
    }

    private static Factura mapFactura(ResultSet rs, Connection conn) throws SQLException {
        Factura factura = new Factura();
        factura.setId(rs.getString("id"));
        Timestamp fecha = rs.getTimestamp("fecha");
        factura.setFecha(fecha != null ? fecha.toLocalDateTime() : null);
        factura.setClienteNombre(rs.getString("cliente_nombre"));
        factura.setClienteIdentificacion(rs.getString("cliente_identificacion"));
        factura.setTipoDocumento(rs.getString("tipo_documento"));
        factura.setClienteEmail(rs.getString("cliente_email"));
        factura.setClienteTelefono(rs.getString("cliente_telefono"));
        factura.setTipoFactura(rs.getString("tipo_factura"));
        factura.setEstado(rs.getString("estado"));
        factura.setEliminada(rs.getBoolean("eliminada"));
        factura.setTotal(rs.getDouble("total"));
        factura.setMetodoPago(rs.getString("metodo_pago"));
        factura.setReferenciaPago(rs.getString("referencia_pago"));

        String pagoJson = rs.getString("pago_json");
        if (pagoJson != null && !pagoJson.isBlank()) {
            try {
                factura.setPago(GSON.fromJson(pagoJson, Pago.class));
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo deserializar el pago de la factura " + factura.getId() + ": " + e.getMessage());
            }
        }

        factura.setProductos(cargarProductosFactura(conn, factura.getId()));
        return factura;
    }

    private static List<Productos> cargarProductosFactura(Connection conn, String facturaId) throws SQLException {
        List<Productos> productos = new ArrayList<>();
        String sql = "SELECT producto_id, nombre_producto, cantidad, precio_unitario FROM factura_detalle WHERE factura_id = ? ORDER BY id";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Productos producto = new Productos();
                    producto.setIdProducto(rs.getString("producto_id"));
                    producto.setNombre(rs.getString("nombre_producto"));
                    producto.setCantidad(rs.getInt("cantidad"));
                    producto.setPrecio(rs.getDouble("precio_unitario"));
                    producto.setPrecioParaVender(rs.getDouble("precio_unitario"));
                    productos.add(producto);
                }
            }
        }

        return productos;
    }

    private static void insertarFactura(Connection conn, Factura factura) throws SQLException {
        String sql = "INSERT INTO facturas (id, fecha, cliente_nombre, cliente_identificacion, tipo_documento, cliente_email, cliente_telefono, tipo_factura, estado, eliminada, total, metodo_pago, referencia_pago, pago_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, factura.getId());
            ps.setTimestamp(2, factura.getFecha() != null ? Timestamp.valueOf(factura.getFecha()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, factura.getClienteNombre());
            ps.setString(4, factura.getClienteIdentificacion());
            ps.setString(5, factura.getTipoDocumento());
            ps.setString(6, factura.getClienteEmail());
            ps.setString(7, factura.getClienteTelefono());
            ps.setString(8, factura.getTipoFactura());
            ps.setString(9, factura.getEstado());
            ps.setBoolean(10, factura.isEliminada());
            ps.setDouble(11, factura.getTotal());
            ps.setString(12, factura.getMetodoPago());
            ps.setString(13, factura.getReferenciaPago());
            ps.setString(14, factura.getPago() != null ? GSON.toJson(factura.getPago()) : null);
            ps.executeUpdate();
        }
    }

    private static void actualizarFacturaCabecera(Connection conn, Factura factura) throws SQLException {
        String sql = "UPDATE facturas SET fecha = ?, cliente_nombre = ?, cliente_identificacion = ?, tipo_documento = ?, cliente_email = ?, cliente_telefono = ?, tipo_factura = ?, estado = ?, eliminada = ?, total = ?, metodo_pago = ?, referencia_pago = ?, pago_json = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, factura.getFecha() != null ? Timestamp.valueOf(factura.getFecha()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, factura.getClienteNombre());
            ps.setString(3, factura.getClienteIdentificacion());
            ps.setString(4, factura.getTipoDocumento());
            ps.setString(5, factura.getClienteEmail());
            ps.setString(6, factura.getClienteTelefono());
            ps.setString(7, factura.getTipoFactura());
            ps.setString(8, factura.getEstado());
            ps.setBoolean(9, factura.isEliminada());
            ps.setDouble(10, factura.getTotal());
            ps.setString(11, factura.getMetodoPago());
            ps.setString(12, factura.getReferenciaPago());
            ps.setString(13, factura.getPago() != null ? GSON.toJson(factura.getPago()) : null);
            ps.setString(14, factura.getId());
            ps.executeUpdate();
        }
    }

    private static void actualizarEstadoFactura(Connection conn, String idFactura, boolean eliminada, String estado) throws SQLException {
        String sql = "UPDATE facturas SET eliminada = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, eliminada);
            ps.setString(2, estado);
            ps.setString(3, idFactura);
            ps.executeUpdate();
        }
    }

    private static void guardarDetalleFactura(Connection conn, String facturaId, List<Productos> productos) throws SQLException {
        String sql = "INSERT INTO factura_detalle (factura_id, producto_id, nombre_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Productos producto : productos) {
                if (producto == null) {
                    continue;
                }
                double precioUnitario = producto.getPrecioParaVender();
                double subtotal = precioUnitario * producto.getCantidad();
                ps.setString(1, facturaId);
                ps.setString(2, producto.getIdProducto());
                ps.setString(3, producto.getNombre());
                ps.setInt(4, producto.getCantidad());
                ps.setDouble(5, precioUnitario);
                ps.setDouble(6, subtotal);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void reemplazarDetalleFactura(Connection conn, String facturaId, List<Productos> productos) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM factura_detalle WHERE factura_id = ?")) {
            delete.setString(1, facturaId);
            delete.executeUpdate();
        }

        if (productos != null && !productos.isEmpty()) {
            guardarDetalleFactura(conn, facturaId, productos);
        }
    }

    private static void ajustarStockPorDiferencia(Connection conn, List<Productos> anteriores, List<Productos> nuevos) throws SQLException {
        Map<String, Integer> cantidadesAnteriores = consolidarCantidades(anteriores);
        Map<String, Integer> cantidadesNuevas = consolidarCantidades(nuevos);
        Map<String, Productos> productosPorId = consolidarProductos(nuevos);

        for (String idProducto : cantidadesAnteriores.keySet()) {
            int anterior = cantidadesAnteriores.getOrDefault(idProducto, 0);
            int nuevo = cantidadesNuevas.getOrDefault(idProducto, 0);
            int diferencia = nuevo - anterior;

            if (diferencia > 0) {
                ProductoService.ajustarStock(conn, idProducto, diferencia, true);
            } else if (diferencia < 0) {
                ProductoService.ajustarStock(conn, idProducto, -diferencia, false);
            }
        }

        for (String idProducto : cantidadesNuevas.keySet()) {
            if (!cantidadesAnteriores.containsKey(idProducto)) {
                int cantidadNueva = cantidadesNuevas.getOrDefault(idProducto, 0);
                if (cantidadNueva > 0) {
                    ProductoService.ajustarStock(conn, idProducto, cantidadNueva, true);
                }
            }
        }

        for (String idProducto : productosPorId.keySet()) {
            if (!cantidadesAnteriores.containsKey(idProducto)) {
                Productos producto = productosPorId.get(idProducto);
                if (producto != null && producto.getCantidad() > 0) {
                    ProductoService.ajustarStock(conn, producto.getIdProducto(), producto.getCantidad(), true);
                }
            }
        }
    }

    private static Map<String, Integer> consolidarCantidades(List<Productos> productos) {
        Map<String, Integer> cantidades = new HashMap<>();
        if (productos == null) {
            return cantidades;
        }

        for (Productos producto : productos) {
            if (producto == null || producto.getIdProducto() == null || producto.getIdProducto().isBlank()) {
                continue;
            }
            cantidades.merge(producto.getIdProducto(), Math.max(0, producto.getCantidad()), Integer::sum);
        }
        return cantidades;
    }

    private static Map<String, Productos> consolidarProductos(List<Productos> productos) {
        Map<String, Productos> map = new HashMap<>();
        if (productos == null) {
            return map;
        }

        for (Productos producto : productos) {
            if (producto == null || producto.getIdProducto() == null || producto.getIdProducto().isBlank()) {
                continue;
            }
            map.putIfAbsent(producto.getIdProducto(), producto);
        }
        return map;
    }

    private static double calcularTotalProductos(List<Productos> productos) {
        if (productos == null) {
            return 0.0;
        }
        return productos.stream()
                .filter(p -> p != null)
                .mapToDouble(p -> p.getPrecioParaVender() * Math.max(0, p.getCantidad()))
                .sum();
    }
}
