module com.proyecto2ev.clientechema {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.proyecto2ev.clientechema to javafx.fxml;
    exports com.proyecto2ev.clientechema;
}