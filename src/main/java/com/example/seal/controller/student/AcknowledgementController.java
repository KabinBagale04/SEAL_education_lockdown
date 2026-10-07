package com.example.seal.controller.student;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class AcknowledgementController extends StudentPage {
    @FXML private Label reference, acknowledgement;
    @FXML private void initialize() {
        setup();
        if (flow.receipt == null) throw new IllegalStateException("A submission acknowledgement is required.");
        reference.setText(flow.receipt.reference()); acknowledgement.setText(flow.receipt.message());
    }
    @FXML private void returnToAccess() { flow.returnToAccess(); }
}
