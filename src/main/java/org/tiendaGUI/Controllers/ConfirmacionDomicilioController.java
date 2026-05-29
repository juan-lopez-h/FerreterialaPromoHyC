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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.tiendaGUI.DTO.DomicilioDTO;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ConfirmacionDomicilioController implements Initializable {

    @FXML private Label lblDireccion;
    @FXML private Label lblReferencia;
    @FXML private Label lblPostal;
    @FXML private Label lblApartamento;
    @FXML private Label lblCelular;
    @FXML private Label lblFechaEntrega;
    @FXML private Label lblFactura;
    @FXML private Button btnContinuar;
    @FXML private Button btnVolver;

    private DomicilioDTO domicilioDTO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ ConfirmacionDomicilioController inicializado");
    }

    /**
     * Recibe el DTO desde DomicilioController y muestra sus datos.
     */
    public void setDomicilioDTO(DomicilioDTO dto) {
        this.domicilioDTO = dto;
        mostrarDatos();
    }

    private void mostrarDatos() {
        if (domicilioDTO == null) {
            System.err.println("No hay datos de domicilio para mostrar");
            return;
        }
        lblDireccion.setText(domicilioDTO.getDireccion());
        lblReferencia.setText(domicilioDTO.getReferenciaDireccion());
        lblPostal.setText(domicilioDTO.getNumeroPostal());
        lblApartamento.setText(domicilioDTO.getNumeroApartamento());
        lblCelular.setText(domicilioDTO.getNumeroCelular());
        lblFechaEntrega.setText(domicilioDTO.getFechaEntrega());
        lblFactura.setText(domicilioDTO.getIdFactura());
    }

    @FXML
    private void btnContinuarAction(ActionEvent event) {
        if (!validarCampos()) return;

        try {
            // Convertir DTO a modelo
            Domicilio domicilio = new Domicilio(
                    domicilioDTO.getDireccion(),
                    domicilioDTO.getReferenciaDireccion(),
                    domicilioDTO.getNumeroPostal(),
                    domicilioDTO.getNumeroApartamento(),
                    domicilioDTO.getNumeroCelular(),
                    LocalDate.parse(domicilioDTO.getFechaEntrega()),
                    domicilioDTO.getIdFactura(),
                    domicilioDTO.getIdFactura() // Usamos el ID de factura como clienteIdentificacion
            );

            // Guardar en MongoDB
            DomicilioService.guardarDomicilio(domicilio);

            // Navegar a la siguiente vista
            navegar(event, "/org/tiendaGUI/pedido-view.fxml", "Carrito de Compras");
            
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al guardar el domicilio: " + e.getMessage()).showAndWait();
        }
    }


    @FXML
    private void btnVolverAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/tiendaGUI/domicilio-view.fxml"));
            Parent root = loader.load();
            DomicilioController ctrl = loader.getController();
            // Pasa DTO de nuevo
            ctrl.setDomicilioDTO(domicilioDTO);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Domicilio");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo volver al formulario").showAndWait();
        }
    }

    private boolean validarCampos() {
        if (domicilioDTO == null) {
            new Alert(Alert.AlertType.ERROR, "No hay datos de domicilio para validar").showAndWait();
            return false;
        }
        
        // Validar número de celular
        if (!Pattern.matches("\\d{10}", domicilioDTO.getNumeroCelular())) {
            new Alert(Alert.AlertType.ERROR, "El celular debe tener 10 dígitos").showAndWait();
            return false;
        }
        
        // Validar fecha de entrega
        try {
            LocalDate fecha = LocalDate.parse(domicilioDTO.getFechaEntrega());
            if (fecha.isBefore(LocalDate.now())) {
                new Alert(Alert.AlertType.ERROR, "La fecha de entrega no puede ser en el pasado").showAndWait();
                return false;
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Formato de fecha inválido").showAndWait();
            return false;
        }
        
        // Validar que exista la factura
        try {
            String idFactura = domicilioDTO.getIdFactura().trim();
            if (FacturaService.buscarFacturaPorId(idFactura) == null) {
                new Alert(Alert.AlertType.ERROR, "No se encontró la factura con ID: " + idFactura).showAndWait();
                return false;
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al validar la factura: " + e.getMessage()).showAndWait();
            return false;
        }
        
        return true;
    }

    private void navegar(ActionEvent event, String fxml, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxml);
            if (fxmlUrl == null) {
                throw new IOException("No se pudo encontrar el archivo FXML: " + fxml);
            }
            
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al navegar: " + e.getMessage()).showAndWait();
        }
    }
}
