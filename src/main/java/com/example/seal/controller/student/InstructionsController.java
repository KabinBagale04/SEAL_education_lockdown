package com.example.seal.controller.student;
import com.example.seal.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class InstructionsController extends StudentPage {
    @FXML private Label title, details, instructions;
    @FXML private CheckBox accepted;
    @FXML private Button start, back;
    @FXML private void initialize() {
        setup();
        if (flow.access == null) throw new IllegalStateException("Exam access must be validated first.");
        title.setText(flow.access.title());
        details.setText(flow.access.durationMinutes() + " minutes  |  " + flow.access.questionCount() + " questions");
        instructions.setText(flow.access.instructions());
        start.setDisable(true);
        accepted.selectedProperty().addListener((o, previous, selected) -> start.setDisable(busy || !selected));
    }
    @FXML private void back() { flow.returnToAccess(); }
    @FXML private void start() {
        if (!accepted.isSelected()) return;
        String token = UserSession.getToken();
        run(() -> flow.service.startExam(flow.access.accessId(), flow.startRequestId, token), attempt -> {
            if (attempt == null || attempt.expiresAt() == null || attempt.questions().isEmpty()) {
                message.setText("The exam service returned an incomplete attempt. Please contact your teacher."); return;
            }
            flow.attempt = attempt; flow.go("examination");
        }, value -> {
            start.setDisable(value || !accepted.isSelected()); back.setDisable(value); accepted.setDisable(value);
            start.setText(value ? "Starting..." : "Start exam");
        });
    }
}
