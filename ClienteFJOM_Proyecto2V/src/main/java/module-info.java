module com.proyecto2v.clientefjom_proyecto2v {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.proyecto2v.clientefjom_proyecto2v to javafx.fxml;
    exports com.proyecto2v.clientefjom_proyecto2v;
}