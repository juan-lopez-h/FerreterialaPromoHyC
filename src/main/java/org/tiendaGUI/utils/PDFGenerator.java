package org.tiendaGUI.utils;

import LogicaTienda.Model.Factura;
import LogicaTienda.Model.Productos;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.io.image.ImageDataFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    private static final DeviceRgb COLOR_ACCENT = new DeviceRgb(63, 81, 181);
    private static final DeviceRgb COLOR_HEADER = new DeviceRgb(13, 71, 161);

    /**
     * Genera un archivo PDF para la factura proporcionada.
     * @param factura La factura a generar en formato PDF
     * @throws IOException Si ocurre un error al crear o escribir el archivo PDF
     * @throws IllegalArgumentException Si la factura es nula
     */
    public static void generarFacturaPDF(Factura factura) throws IOException {
        generarFacturaPDF(factura, false);
    }

    /**
     * Genera un archivo PDF para la factura proporcionada.
     * @param factura La factura a generar en formato PDF
     * @param overwrite Si es true, sobrescribe el archivo si ya existe
     * @throws IOException Si ocurre un error al crear o escribir el archivo PDF
     * @throws IllegalArgumentException Si la factura es nula
     */
    public static void generarFacturaPDF(Factura factura, boolean overwrite) throws IOException {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula");
        }

        // Obtener carpeta Descargas según SO
        String userHome = System.getProperty("user.home");
        File downloads = new File(userHome, "Downloads"); // Windows, Mac
        if (!downloads.exists() || !downloads.isDirectory()) {
            downloads = new File(userHome, "Descargas"); // Linux en español
        }

        // Crear directorio de descargas si no existe
        if ((!downloads.exists() || !downloads.isDirectory()) && !downloads.mkdirs()) {
            throw new IOException("No se pudo crear el directorio de descargas: " + downloads.getAbsolutePath());
        }

        // Determinar el nombre del archivo según el tipo de factura
        String fileName = "FACTURA_" + (factura.getId() != null ? factura.getId() : "SIN_ID") + ".pdf";
        if (factura.getTipoFactura() != null && factura.getTipoFactura().equals("Cotizacion")) {
            fileName = "COTIZACION_" + (factura.getId() != null ? factura.getId() : "SIN_ID") + ".pdf";
        }

        // Crear archivo de salida
        File file = new File(downloads, fileName);

        // Verificar si el archivo ya existe
        if (file.exists()) {
            if (overwrite) {
                // Intentar eliminar el archivo existente
                if (!file.delete()) {
                    throw new IOException("No se pudo eliminar el archivo existente: " + file.getAbsolutePath());
                }
            } else {
                throw new IOException("El archivo ya existe: " + file.getAbsolutePath());
            }
        }

        // Inicializar recursos
        PdfWriter writer = null;
        PdfDocument pdf = null;
        Document document = null;

        try {
            // Crear escritor, pdf y documento
            writer = new PdfWriter(file.getAbsolutePath());
            pdf = new PdfDocument(writer);
            document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 36, 36, 36);

            // Fuentes
            PdfFont fontBold = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // Encabezado
            Table header = new Table(2).useAllAvailableWidth();

            // Cargar logo
            String logoPath = PDFGenerator.class.getResource("/org/tiendaGUI/images/LogoFerreteria.png").getPath();
            Image logo = new Image(ImageDataFactory.create(logoPath))
                    .setWidth(100)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT);

            // Información de la empresa
            Cell empresaCell = new Cell().setBorder(null);
            empresaCell.add(logo);

            Paragraph empresa = new Paragraph()
                    .add("FERRETERÍA LA PROMO H&C\n").setFont(fontBold).setFontSize(16).setFontColor(COLOR_ACCENT)
                    .add(" LAURA YAMILE HERNANDEZ \n").setFont(fontBold).setFontSize(16).setFontColor(COLOR_ACCENT)
                    .add("NIT: 37749393-1\n").setFont(fontNormal).setFontSize(10)
                    .add("Régimen: Régimen Simplificado\n").setFont(fontNormal).setFontSize(10)
                    .add("Actividad: 4649\n").setFont(fontNormal).setFontSize(10)
                    .add("Dirección: Manzana 27 #casa 16 local 1, Armenia\n").setFont(fontNormal).setFontSize(10)
                    .add("Teléfono: 323 3934257\n").setFont(fontNormal).setFontSize(10)
                    .add("Email: juanlopezcastillooo@gmail.com").setFont(fontNormal).setFontSize(10);

            empresaCell.add(empresa);
            header.addCell(empresaCell);

            // Información de la factura
            String fechaStr = factura.getFecha() != null
                    ? factura.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    : "Fecha no disponible";

            String facturaId = factura.getId() != null ? factura.getId() : "SIN_ID";

            // Determinar el tipo de documento (factura o cotización)
            String tipoDocumento = "FACTURA DE VENTA";
            String prefijo = "No. ";

            if (factura.getTipoFactura() != null && factura.getTipoFactura().equals("Cotizacion")) {
                tipoDocumento = "COTIZACIÓN";
                prefijo = "Cotización No. ";
            }

            Paragraph facturaInfo = new Paragraph()
                    .add(tipoDocumento + "\n").setFont(fontBold).setFontSize(16).setFontColor(COLOR_ACCENT)
                    .add(prefijo + facturaId + "\n").setFont(fontBold).setFontSize(12)
                    .add("Fecha: " + fechaStr).setFont(fontNormal).setFontSize(10);

            header.addCell(new Cell().add(facturaInfo).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

            document.add(header);
            document.add(new LineSeparator(new SolidLine()).setMarginTop(10).setMarginBottom(10));

            // Información del cliente
            document.add(new Paragraph("INFORMACIÓN DEL CLIENTE").setFont(fontBold).setFontSize(12).setFontColor(COLOR_HEADER));

            Table clientTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
            clientTable.setWidth(UnitValue.createPercentValue(60));

            // Validar y mostrar información del cliente
            String clienteNombre = factura.getClienteNombre() != null ? factura.getClienteNombre() : "No especificado";
            String clienteId = factura.getClienteIdentificacion() != null ? factura.getClienteIdentificacion() : "No especificada";

            clientTable.addCell(createCell("Nombre:", fontBold, 10, null, TextAlignment.LEFT));
            clientTable.addCell(createCell(clienteNombre, fontNormal, 10, null, TextAlignment.LEFT));
            clientTable.addCell(createCell("Identificación:", fontBold, 10, null, TextAlignment.LEFT));
            clientTable.addCell(createCell(clienteId, fontNormal, 10, null, TextAlignment.LEFT));

            document.add(clientTable);
            document.add(new Paragraph("\n"));

            // Sección de productos
            document.add(new Paragraph("DETALLE DE LA COMPRA").setFont(fontBold).setFontSize(12).setFontColor(COLOR_HEADER));

            // Configuración de la tabla de productos
            float[] colWidths = {3, 1, 1.5f, 1.5f};
            Table productsTable = new Table(colWidths);
            productsTable.setWidth(UnitValue.createPercentValue(100));

            // Encabezados de la tabla
            String[] headers = {"DESCRIPCIÓN", "CANT.", "V. UNITARIO", "TOTAL"};
            for (String h : headers) {
                productsTable.addHeaderCell(createCell(
                    h, 
                    fontBold, 
                    10, 
                    new DeviceRgb(240, 240, 240), 
                    TextAlignment.CENTER
                ));
            }

            // Validar y mostrar productos
            List<Productos> productos = factura.getProductos();
            if (productos == null || productos.isEmpty()) {
                // Mostrar mensaje si no hay productos
                productsTable.addCell(new Cell(1, 4)
                    .add(new Paragraph("No hay productos en esta factura")
                        .setFont(fontNormal)
                        .setTextAlignment(TextAlignment.CENTER)));
            } else {
                // Calcular totales
                double subtotal = 0;

                // Agregar cada producto a la tabla
                for (Productos p : productos) {
                    if (p == null) continue;

                    String nombre = p.getNombre() != null ? p.getNombre() : "Producto sin nombre";
                    int cantidad = Math.max(0, p.getCantidad());
                    double valorUnitario = p.getPrecioParaVender();
                    double total = valorUnitario * cantidad;
                    subtotal += total;

                    productsTable.addCell(createCell(nombre, fontNormal, 9, null, TextAlignment.LEFT));
                    productsTable.addCell(createCell(String.valueOf(cantidad), fontNormal, 9, null, TextAlignment.CENTER));
                    productsTable.addCell(createCell(formatMoney(valorUnitario), fontNormal, 9, null, TextAlignment.RIGHT));
                    productsTable.addCell(createCell(formatMoney(total), fontNormal, 9, null, TextAlignment.RIGHT));
                }

                // Agregar fila de subtotal
                productsTable.addCell(new Cell(1, 3)
                    .add(new Paragraph("SUBTOTAL:")
                        .setFont(fontBold)
                        .setTextAlignment(TextAlignment.RIGHT)));
                productsTable.addCell(createCell(formatMoney(subtotal), fontBold, 10, null, TextAlignment.RIGHT));

                // Agregar fila de total
                double totalFactura = subtotal;
                productsTable.addCell(new Cell(1, 3)
                    .add(new Paragraph("TOTAL:")
                        .setFont(fontBold)
                        .setFontSize(11)
                        .setTextAlignment(TextAlignment.RIGHT)));
                productsTable.addCell(createCell(formatMoney(totalFactura), fontBold, 11, null, TextAlignment.RIGHT));
            }

            document.add(productsTable);

            // Agregar mensaje de agradecimiento o mensaje de cotización
            document.add(new Paragraph("\n"));

            String mensaje = "¡Gracias por su compra!";
            if (factura.getTipoFactura() != null && factura.getTipoFactura().equals("Cotizacion")) {
                mensaje = "Esta cotización tiene una validez de 15 días a partir de la fecha de emisión.";
            }

            document.add(new Paragraph(mensaje)
                .setFont(fontNormal)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setMarginTop(10));

            // Pie de página
            document.add(new Paragraph("\n\n"));
            document.add(new LineSeparator(new SolidLine()));

            String fechaGeneracion = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            String tipoDoc = "Factura";
            if (factura.getTipoFactura() != null && factura.getTipoFactura().equals("Cotizacion")) {
                tipoDoc = "Cotización";
            }

            Paragraph footer = new Paragraph()
                    .add("FERRETERÍA LA PROMO - NIT: 37749393-1\n")
                    .add("Dirección: Manzana 27 #casa 16 local 1, Armenia - Teléfono: 323 3934257\n")
                    .add("Email: juanlopezcastillooo@gmail.com - " + 
                         tipoDoc + " generada el " + fechaGeneracion)
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER);

            document.add(footer);

            // Cerrar documento
            document.close();

            // Confirmación de creación
            String mensajeExito = overwrite ? "✅ Factura PDF regenerada exitosamente en: " : "✅ Factura PDF generada exitosamente en: ";
            System.out.println(mensajeExito + file.getAbsolutePath());

        } catch (Exception e) {
            // Cerrar recursos en caso de error
            if (document != null && document.getPdfDocument() != null) {
                document.close();
            } else if (pdf != null) {
                pdf.close();
            } else if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    // Ignorar error al cerrar
                }
            }

            // Eliminar archivo parcial si existe
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    System.err.println("⚠️ No se pudo eliminar el archivo temporal: " + file.getAbsolutePath());
                }
            }

            // Relanzar la excepción para que el llamador pueda manejarla
            throw new IOException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una celda con el texto formateado para las tablas del PDF.
     * @param text Texto a mostrar en la celda
     * @param font Fuente a utilizar
     * @param fontSize Tamaño de la fuente
     * @param backgroundColor Color de fondo (opcional)
     * @param alignment Alineación del texto
     * @return Celda configurada
     */
    private static Cell createCell(String text, PdfFont font, float fontSize, DeviceRgb backgroundColor, TextAlignment alignment) {
        if (text == null) {
            text = "";
        }
        Paragraph p = new Paragraph(text)
                .setFont(font)
                .setFontSize(fontSize);

        Cell cell = new Cell()
                .add(p)
                .setBorder(null);

        if (alignment != null) {
            cell.setTextAlignment(alignment);
        }

        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
        }

        return cell;
    }

    /**
     * Formatea un valor numérico como moneda con separador de miles y 2 decimales.
     * @param amount Cantidad a formatear
     * @return Cadena formateada como moneda
     */
    private static String formatMoney(double amount) {
        Locale locale = new Locale.Builder()
                .setLanguage("es")
                .setRegion("CO")
                .build();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        return df.format(amount);
    }
}
