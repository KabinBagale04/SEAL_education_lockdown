package com.example.seal.controller.admin;

import com.example.seal.Navigator;
import com.example.seal.navigation.AdminNavigator;
import com.example.seal.service.AuthService;
import com.example.seal.session.UserSession;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.awt.*;

public class AdminShellController {
    @FXML
    private VBox sidebar;
    @FXML
    private Button userButton;
    @FXML
    private Button addUserButton;
    @FXML
    private StackPane contentArea;

    private boolean sidebarCollapsed = false;

    private void setSidebarWidth(double width){
        sidebar.setMinWidth(width);
        sidebar.setPrefWidth(width);
        sidebar.setMaxWidth(width);
    }

    @FXML
    private void initialize(){
        AdminNavigator.setContentArea(contentArea);
        AdminNavigator.goTo("users");
    }
    @FXML
    private void toggleSidebar(){
        sidebarCollapsed = !sidebarCollapsed;
        if(sidebarCollapsed){
            setSidebarWidth(75);
            userButton.setText("U");
            addUserButton.setText("+");

            userButton.setAlignment(Pos.CENTER);
            addUserButton.setAlignment(Pos.CENTER);
        }else {
            setSidebarWidth(210);
            userButton.setText("Users");
            addUserButton.setText("+ Add User");

            userButton.setAlignment(Pos.CENTER_LEFT);
            addUserButton.setAlignment(Pos.CENTER_LEFT);
        }
    }

    @FXML
private void handleLogout(){ com.example.seal.service.SessionActions.logout(); }

    @FXML
    private void showAddUser(){
        AdminNavigator.goTo("add-user");
    }
    @FXML
    private void showUsers(){
        AdminNavigator.goTo("users");
    }
    private final AuthService authService = new AuthService();
}
