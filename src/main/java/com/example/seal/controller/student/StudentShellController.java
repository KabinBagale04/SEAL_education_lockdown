package com.example.seal.controller.student;

import com.example.seal.navigation.StudentNavigator;
import com.example.seal.service.SessionActions;
import com.example.seal.session.UserSession;
import com.example.seal.student.StudentFlow;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

public final class StudentShellController {
    @FXML private StackPane contentArea;
    @FXML private Label fullName;
    @FXML private ToggleButton dashboardButton, examsButton, resultsButton;

    @FXML private void initialize() {
        StudentFlow.current();
        fullName.setText(UserSession.getFullName());
        StudentNavigator.attach(contentArea, page -> {
            dashboardButton.setSelected("dashboard".equals(page));
            examsButton.setSelected("access".equals(page));
            resultsButton.setSelected("results".equals(page));
        });
        StudentNavigator.goTo("dashboard");
    }

    @FXML private void dashboard() { StudentNavigator.goTo("dashboard"); }
    @FXML private void exams() { StudentNavigator.goTo("access"); }
    @FXML private void results() { StudentNavigator.goTo("results"); }
    @FXML private void logout() { SessionActions.logout(); }
}
