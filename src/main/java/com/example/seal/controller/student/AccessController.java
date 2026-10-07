package com.example.seal.controller.student;
import com.example.seal.session.UserSession;
import com.example.seal.student.model.ExamDtos.AccessRequest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public final class AccessController extends StudentPage {
    @FXML private TextField registration, symbol, code;
    @FXML private Button continueButton;
    @FXML private VBox fields;
    @FXML private void initialize() {
        setup();
        for (TextField field : new TextField[]{registration, symbol, code})
            field.textProperty().addListener((o, before, after) -> message.setText(""));
    }
    @FXML private void validate() {
        if (registration.getText().isBlank() || symbol.getText().isBlank() || code.getText().isBlank()) {
            message.setText("Enter your registration number, symbol number and exam access code."); return;
        }
        AccessRequest request = new AccessRequest(registration.getText().trim(), symbol.getText().trim(), code.getText().trim());
        String token = UserSession.getToken();
        run(() -> flow.service.validateAccess(request, token), access -> {
            flow.reset(); flow.access = access; flow.go("instructions");
        }, value -> {
            fields.setDisable(value); continueButton.setDisable(value);
            continueButton.setText(value ? "Validating..." : "Continue");
        });
    }
}
