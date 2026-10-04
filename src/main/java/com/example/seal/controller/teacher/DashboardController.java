package com.example.seal.controller.teacher;

import com.example.seal.navigation.TeacherNavigator;
import javafx.fxml.FXML;

public class DashboardController {
    @FXML
    private void createExam(){
        TeacherNavigator.goTo("create-exam");
    }
}
