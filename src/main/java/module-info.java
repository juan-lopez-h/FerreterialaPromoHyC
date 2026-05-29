module ferreteria.la.promo {
    // Módulos JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;

    // Módulos de utilidades
    requires org.controlsfx.controls;
    requires com.google.gson;
    requires static lombok;
    requires java.desktop;
    requires java.logging;
    requires java.sql;

    // Módulos iText para generación de PDF
    requires kernel;
    requires layout;
    requires io;
    requires forms;
    requires hyph;

    // Exportar paquetes públicos
    exports app;
    
    // Exportar paquetes de modelo y utilidades
    exports LogicaTienda.Model to javafx.fxml, com.google.gson;
    exports LogicaTienda.Utils to javafx.fxml, com.google.gson;
    exports LogicaTienda.Services to javafx.fxml, com.google.gson;
    exports LogicaTienda.Data to javafx.fxml, com.google.gson;

    // Exportar paquetes de la interfaz gráfica
    exports org.tiendaGUI.Controllers to javafx.fxml;
    exports org.tiendaGUI.DTO to javafx.fxml, com.google.gson;
    exports org.tiendaGUI.utils to javafx.fxml, com.google.gson;

    // Abrir paquetes para reflexión (necesarios para JavaFX, Gson y FXML)
    opens app to javafx.fxml, javafx.graphics, com.google.gson;
    opens LogicaTienda to javafx.fxml, com.google.gson;
    opens LogicaTienda.Model to javafx.fxml, com.google.gson, javafx.base;
    opens LogicaTienda.Utils to javafx.fxml, com.google.gson;
    opens LogicaTienda.Services to javafx.fxml, com.google.gson;
    opens LogicaTienda.Data to javafx.fxml, com.google.gson;
    opens org.tiendaGUI.Controllers to javafx.fxml, com.google.gson, javafx.graphics;
    opens org.tiendaGUI.DTO to javafx.fxml, com.google.gson, javafx.base;
    opens org.tiendaGUI.utils to javafx.fxml, com.google.gson;
}
