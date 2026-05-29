package org.tiendaGUI.Controllers;

import LogicaTienda.Model.Domicilio;
import LogicaTienda.Services.DomicilioService;
import LogicaTienda.Services.FacturaService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import org.tiendaGUI.Controllers.loader.ViewLoader;
import org.tiendaGUI.DTO.DomicilioDTO;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DomicilioController implements Initializable {

    @FXML private TextField txtDireccion;
    @FXML private TextField txtReferenciaDireccion;
    @FXML private TextField txtNumeroPostal;
    @FXML private TextField txtNumeroApartamento;
    @FXML private TextField txtNumeroCelular;
    @FXML private DatePicker dpFechaEntrega;
    @FXML private TextField txtIdFactura;

    @FXML private Button btnEnviar;
    @FXML private Button btnVolver;
    private DomicilioDTO domicilioDTO;
    @Setter
    private List<String> idFacturasValidas = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cargar IDs de facturas válidas desde MongoDB
        FacturaService.obtenerTodasLasFacturas().forEach(factura -> {
            if (factura.getId() != null) {
                idFacturasValidas.add(factura.getId().trim().toLowerCase());
            }
        });

        System.out.println("📦 Inicializando controlador de domicilio.");
        System.out.println("IDs de factura válidos: " + idFacturasValidas);

        if (dpFechaEntrega != null) {
            dpFechaEntrega.setValue(LocalDate.now().plusDays(1));
        }
    }

    public void setDomicilioDTO(DomicilioDTO dto) {
        this.domicilioDTO = dto;
        txtDireccion.setText(dto.getDireccion());
        txtReferenciaDireccion.setText(dto.getReferenciaDireccion());
        txtNumeroPostal.setText(dto.getNumeroPostal());
        txtNumeroApartamento.setText(dto.getNumeroApartamento());
        txtNumeroCelular.setText(dto.getNumeroCelular());
        dpFechaEntrega.setValue(LocalDate.parse(dto.getFechaEntrega()));
        txtIdFactura.setText(dto.getIdFactura());
    }

    @FXML
    private void btnEnviarAction(ActionEvent event) {
        if (!validarFormulario()) return;

        DomicilioDTO dto = new DomicilioDTO(
                txtDireccion.getText(),
                txtReferenciaDireccion.getText(),
                txtNumeroPostal.getText(),
                txtNumeroApartamento.getText(),
                txtNumeroCelular.getText(),
                dpFechaEntrega.getValue().toString(),
                txtIdFactura.getText()
        );
        
        // Guardar el domicilio en MongoDB
        guardarDomicilio(dto);
        irAVistaConfirmacion(event, dto);
    }

    private void irAVistaConfirmacion(ActionEvent event, DomicilioDTO dto) {
        try {
            URL fxmlUrl = getClass().getResource("/org/tiendaGUI/domicilio-confirmacion-view.fxml");
            if (fxmlUrl == null) {
                throw new IOException("No se pudo encontrar el archivo FXML");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            ConfirmacionDomicilioController controller = loader.getController();
            controller.setDomicilioDTO(dto);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Confirmación de Domicilio");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo cargar la vista de confirmación: " + e.getMessage());
        }
    }

    @FXML
    private void btnVolverAction(ActionEvent event) {
        cambiarVentana(event);
    }

    private boolean validarFormulario() {
        if (txtDireccion.getText().trim().isEmpty()) {
            mostrarAlerta("Debe ingresar una dirección");
            return false;
        }

        if (!Pattern.matches("\\d{10}", txtNumeroCelular.getText().trim())) {
            mostrarAlerta("El número de celular debe tener 10 dígitos");
            return false;
        }

        if (dpFechaEntrega.getValue() == null || dpFechaEntrega.getValue().isBefore(LocalDate.now())) {
            mostrarAlerta("La fecha de entrega debe ser hoy o futura");
            return false;
        }

        // ✅ Validar ID de factura normalizado
        String idFacturaIngresado = txtIdFactura.getText().trim().toLowerCase();
        System.out.println("🔍 ID ingresado: " + idFacturaIngresado);
        if (!idFacturasValidas.contains(idFacturaIngresado)) {
            mostrarAlerta("El ID de factura no es válido.");
            return false;
        }

        return true;
    }

    private void guardarDomicilio(DomicilioDTO dto) {
        try {
            // Convertir DTO a entidad de dominio
            // Usamos el ID de factura como clienteIdentificacion temporal
            Domicilio domicilio = new Domicilio(
                dto.getDireccion(),
                dto.getReferenciaDireccion(),
                dto.getNumeroPostal(),
                dto.getNumeroApartamento(),
                dto.getNumeroCelular(),
                LocalDate.parse(dto.getFechaEntrega()),
                dto.getIdFactura(),
                dto.getIdFactura() // Usamos el ID de factura como clienteIdentificacion
            );
            
            // Guardar en MongoDB
            DomicilioService.guardarDomicilio(domicilio);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al guardar el domicilio: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cambiarVentana(ActionEvent event) {
        ViewLoader.cargarVista(event, "pedido-view.fxml", "Carrito de Compras");
    }
}
