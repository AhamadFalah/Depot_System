module com.example.depot_system {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.depot_system to javafx.fxml;
    exports com.example.depot_system;
}