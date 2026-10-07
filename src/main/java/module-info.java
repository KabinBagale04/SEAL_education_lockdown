module com.example.seal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.example.seal.dto to com.fasterxml.jackson.databind;
    opens com.example.seal.controller to javafx.fxml;
    exports com.example.seal;
    opens com.example.seal.controller.teacher to javafx.fxml;
    opens com.example.seal.controller.admin to javafx.fxml;
    opens com.example.seal.controller.student to javafx.fxml;
}
