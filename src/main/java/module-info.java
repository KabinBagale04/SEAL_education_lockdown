module com.example.seal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.seal.controller to javafx.fxml;
    exports com.example.seal;
    opens com.example.seal.controller.teacher to javafx.fxml;
}