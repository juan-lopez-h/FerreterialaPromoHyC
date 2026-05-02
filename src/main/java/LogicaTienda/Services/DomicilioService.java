package LogicaTienda.Services;

import LogicaTienda.Data.H2Database;
import LogicaTienda.Model.Domicilio;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DomicilioService {
    private DomicilioService() {
    }

    public static List<Domicilio> obtenerTodosLosDomicilios() {
        List<Domicilio> domicilios = new ArrayList<>();
        String sql = "SELECT id, direccion, referencia_direccion, numero_postal, numero_apartamento, numero_celular, fecha_entrega, id_factura, cliente_identificacion, estado_domicilio FROM domicilios ORDER BY fecha_entrega DESC, id DESC";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                domicilios.add(mapDomicilio(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener domicilios: " + e.getMessage());
        }

        return domicilios;
    }

    public static Domicilio buscarDomicilioPorId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        String sql = "SELECT id, direccion, referencia_direccion, numero_postal, numero_apartamento, numero_celular, fecha_entrega, id_factura, cliente_identificacion, estado_domicilio FROM domicilios WHERE id = ?";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDomicilio(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar domicilio por ID: " + e.getMessage());
        }

        return null;
    }

    public static List<Domicilio> buscarDomiciliosPorFactura(String idFactura) {
        if (idFactura == null || idFactura.isBlank()) {
            return new ArrayList<>();
        }

        List<Domicilio> domicilios = new ArrayList<>();
        String sql = "SELECT id, direccion, referencia_direccion, numero_postal, numero_apartamento, numero_celular, fecha_entrega, id_factura, cliente_identificacion, estado_domicilio FROM domicilios WHERE id_factura = ? ORDER BY fecha_entrega DESC";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    domicilios.add(mapDomicilio(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar domicilios por factura: " + e.getMessage());
        }

        return domicilios;
    }

    public static String guardarDomicilio(Domicilio domicilio) {
        if (domicilio == null) {
            return null;
        }

        if (domicilio.getId() == null || domicilio.getId().isBlank()) {
            domicilio.setId(UUID.randomUUID().toString().substring(0, 8));
        }

        if (buscarDomicilioPorId(domicilio.getId()) != null) {
            actualizarDomicilio(domicilio);
        } else {
            insertarDomicilio(domicilio);
        }

        return domicilio.getId();
    }

    public static void actualizarDomicilio(Domicilio domicilio) {
        if (domicilio == null || domicilio.getId() == null || domicilio.getId().isBlank()) {
            return;
        }

        String sql = "UPDATE domicilios SET direccion = ?, referencia_direccion = ?, numero_postal = ?, numero_apartamento = ?, numero_celular = ?, fecha_entrega = ?, id_factura = ?, cliente_identificacion = ?, estado_domicilio = ? WHERE id = ?";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindDomicilio(ps, domicilio, false);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ No se pudo actualizar el domicilio: " + e.getMessage());
        }
    }

    public static void eliminarDomicilio(String id) {
        if (id == null || id.isBlank()) {
            return;
        }

        String sql = "DELETE FROM domicilios WHERE id = ?";
        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ No se pudo eliminar el domicilio: " + e.getMessage());
        }
    }

    public static List<Domicilio> buscarDomiciliosPorCliente(String clienteIdentificacion) {
        if (clienteIdentificacion == null || clienteIdentificacion.isBlank()) {
            return new ArrayList<>();
        }

        List<Domicilio> domicilios = new ArrayList<>();
        String sql = "SELECT id, direccion, referencia_direccion, numero_postal, numero_apartamento, numero_celular, fecha_entrega, id_factura, cliente_identificacion, estado_domicilio FROM domicilios WHERE cliente_identificacion = ? ORDER BY fecha_entrega DESC";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clienteIdentificacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    domicilios.add(mapDomicilio(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar domicilios por cliente: " + e.getMessage());
        }

        return domicilios;
    }

    private static void insertarDomicilio(Domicilio domicilio) {
        String sql = "INSERT INTO domicilios (id, direccion, referencia_direccion, numero_postal, numero_apartamento, numero_celular, fecha_entrega, id_factura, cliente_identificacion, estado_domicilio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = H2Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindDomicilio(ps, domicilio, true);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ No se pudo guardar el domicilio: " + e.getMessage());
        }
    }

    private static void bindDomicilio(PreparedStatement ps, Domicilio domicilio, boolean incluirIdPrimero) throws SQLException {
        int index = 1;
        if (incluirIdPrimero) {
            ps.setString(index++, domicilio.getId());
        }

        ps.setString(index++, domicilio.getDireccion());
        ps.setString(index++, domicilio.getReferenciaDireccion());
        ps.setString(index++, domicilio.getNumeroPostal());
        ps.setString(index++, domicilio.getNumeroApartamento());
        ps.setString(index++, domicilio.getNumeroCelular());
        if (domicilio.getFechaEntrega() != null) {
            ps.setDate(index++, Date.valueOf(domicilio.getFechaEntrega()));
        } else {
            ps.setDate(index++, null);
        }
        ps.setString(index++, domicilio.getIdFactura());
        ps.setString(index++, domicilio.getClienteIdentificacion());
        ps.setString(index++, domicilio.getEstadoDomicilio());
        if (!incluirIdPrimero) {
            ps.setString(index, domicilio.getId());
        }
    }

    private static Domicilio mapDomicilio(ResultSet rs) throws SQLException {
        Domicilio domicilio = new Domicilio();
        domicilio.setId(rs.getString("id"));
        domicilio.setDireccion(rs.getString("direccion"));
        domicilio.setReferenciaDireccion(rs.getString("referencia_direccion"));
        domicilio.setNumeroPostal(rs.getString("numero_postal"));
        domicilio.setNumeroApartamento(rs.getString("numero_apartamento"));
        domicilio.setNumeroCelular(rs.getString("numero_celular"));
        Date fechaEntrega = rs.getDate("fecha_entrega");
        domicilio.setFechaEntrega(fechaEntrega != null ? fechaEntrega.toLocalDate() : null);
        domicilio.setIdFactura(rs.getString("id_factura"));
        domicilio.setClienteIdentificacion(rs.getString("cliente_identificacion"));
        domicilio.setEstadoDomicilio(rs.getString("estado_domicilio"));
        return domicilio;
    }
}
