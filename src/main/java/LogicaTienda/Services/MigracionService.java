package LogicaTienda.Services;

import LogicaTienda.Model.Productos;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio para la siembra inicial de datos en la aplicación
 */
public class MigracionService {
    private static final Logger LOGGER = Logger.getLogger(MigracionService.class.getName());

    public static void migrarDatos() {
        try {
            LOGGER.info("Iniciando verificación de datos iniciales...");

            List<Productos> productosExistentes = ProductoService.obtenerTodosLosProductos();
            if (productosExistentes.isEmpty()) {
                LOGGER.info("Creando datos iniciales para la base local...");
                crearProductosIniciales();
            } else {
                LOGGER.info("La base local ya contiene productos. Se omite la siembra inicial.");
            }

            LOGGER.info("✅ Verificación de datos iniciales completada con éxito");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error durante la verificación de datos iniciales", e);
            throw new RuntimeException("Error durante la verificación de datos iniciales: " + e.getMessage(), e);
        }
    }

    private static void crearProductosIniciales() {
        List<Productos> productosIniciales = new ArrayList<>();

        productosIniciales.add(new Productos("P001", "Martillo", 15000, 20, 10));
        productosIniciales.add(new Productos("P002", "Destornillador Phillips", 8000, 25, 20));
        productosIniciales.add(new Productos("P003", "Llave Inglesa 12\"", 25000, 15, 5));
        productosIniciales.add(new Productos("P004", "Cinta Métrica 5m", 12000, 30, 15));
        productosIniciales.add(new Productos("P005", "Sierra Manual", 18000, 20, 8));

        try {
            for (Productos producto : productosIniciales) {
                ProductoService.guardarProducto(producto);
            }
            LOGGER.info("✅ Se han creado " + productosIniciales.size() + " productos iniciales");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al insertar productos iniciales", e);
            throw new RuntimeException("Error al crear productos iniciales", e);
        }
    }
}
