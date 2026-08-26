package org.tiendaGUI.utils;

import LogicaTienda.Model.Factura;
import LogicaTienda.Model.Productos;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PDFGeneratorTest {

    @Test
    void generarFacturaPDF_creaArchivoParaCotizacion() throws Exception {
        Factura factura = new Factura();
        factura.setId("TEST123");
        factura.setTipoFactura("Cotizacion");
        factura.setFecha(LocalDateTime.now());
        factura.setClienteNombre("Cliente de prueba");
        factura.setClienteIdentificacion("CC 123456789");
        factura.setProductos(List.of(new Productos("P1", "Martillo", 25000, 2)));

        Path downloadsDir = Path.of(System.getProperty("user.home"), "Downloads");
        if (!Files.exists(downloadsDir)) {
            downloadsDir = Path.of(System.getProperty("user.home"), "Descargas");
        }

        Path pdfPath = downloadsDir.resolve("COTIZACION_TEST123.pdf");
        Files.deleteIfExists(pdfPath);

        assertDoesNotThrow(() -> PDFGenerator.generarFacturaPDF(factura, true));
        assertTrue(Files.exists(pdfPath), "El PDF de la cotización debe generarse en la carpeta de descargas");

        Files.deleteIfExists(pdfPath);
    }
}
