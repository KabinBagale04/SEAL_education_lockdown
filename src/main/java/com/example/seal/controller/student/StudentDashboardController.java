package com.example.seal.controller.student;

import com.example.seal.navigation.StudentNavigator;
import javafx.fxml.FXML;

public final class StudentDashboardController {
    @FXML private void exams() { StudentNavigator.goTo("access"); }
    @FXML private void results() { StudentNavigator.goTo("results"); }
}
