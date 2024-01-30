module com.example.serverchemayjavi {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.serverchemayjavi to javafx.fxml;
    exports com.example.serverchemayjavi;
}