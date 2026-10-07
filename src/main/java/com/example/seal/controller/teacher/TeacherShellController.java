package com.example.seal.controller.teacher;

import com.example.seal.Navigator;
import com.example.seal.navigation.TeacherNavigator;
import com.example.seal.session.UserSession;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


public class TeacherShellController {

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox sidebar;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button examsButton;
    @FXML
    private Button resultsButton;

    private boolean sidebarCollapsed = false;


    @FXML
    private void initialize() {
        TeacherNavigator.setContentArea(contentArea);
        TeacherNavigator.goTo("dashboard");
    }

    @FXML
    private void showDashboard() {
        TeacherNavigator.goTo("dashboard");
    }

    @FXML
    private void showExams() {
        TeacherNavigator.goTo("exams");
    }

    @FXML
    private void showResults() {
        TeacherNavigator.goTo("results");
    }
    @FXML
    private void toggleSidebar(){
        sidebarCollapsed = !sidebarCollapsed;
        if(sidebarCollapsed){
            setSidebarWidth(75);

            dashboardButton.setText("D");
            examsButton.setText("E");
            resultsButton.setText("R");

            dashboardButton.setAlignment(Pos.CENTER);
            examsButton.setAlignment(Pos.CENTER);
            resultsButton.setAlignment(Pos.CENTER);
        }else{
            setSidebarWidth(210);
            dashboardButton.setText("Dashboard");
            examsButton.setText("Exams");
            resultsButton.setText("Results");

            dashboardButton.setAlignment(Pos.CENTER_LEFT);
            examsButton.setAlignment(Pos.CENTER_LEFT);
            resultsButton.setAlignment(Pos.CENTER_LEFT);
        }
    }

    //so yo introduce gareko, cause bug aayo sidebar me so this function makes things standard mathi collapsed ra expanded bata call garxa
    private void setSidebarWidth(double width){
        sidebar.setMinWidth(width);
        sidebar.setPrefWidth(width);
        sidebar.setMaxWidth(width);
    }

    @FXML
private void handleLogout(){ com.example.seal.service.SessionActions.logout(); }
}
