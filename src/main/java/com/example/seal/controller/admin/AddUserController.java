package com.example.seal.controller.admin;

import com.example.seal.dto.CreateUserRequest;
import com.example.seal.dto.UserResponse;
import com.example.seal.navigation.AdminNavigator;
import com.example.seal.service.AdminService;
import com.example.seal.session.UserSession;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;


public class AddUserController {
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label messageLabel;

    private final AdminService adminService = new AdminService();
    private final PauseTransition messageTimer = new PauseTransition(Duration.seconds(3));

    @FXML
    private void initialize(){
        roleComboBox.getItems().addAll("TEACHER",
                "STUDENT");
        messageTimer.setOnFinished(event -> {
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
            messageLabel.setText("");
        });
    }

    @FXML
    private void handleCreateUser(){
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if(fullName.isEmpty() ||
        username.isEmpty() ||
        password.isEmpty() ||
        role == null){
            showMessage("Please complete all the fields",false);
            return;
        }

        CreateUserRequest request = new CreateUserRequest(
                fullName,
                username,
                password,
                role
        );
        String token = UserSession.getToken();

        Task<UserResponse> createUserTask = new Task<>(){
            @Override
            protected UserResponse call() throws Exception{
                return adminService.createUser(request,token);
            }
        };

        createUserTask.setOnSucceeded(event->{
            UserResponse createdUser = createUserTask.getValue();
            showMessage(
                    createdUser.fullName()+" created successfully.",true
            );
            clearForm();
        });

        createUserTask.setOnFailed(event -> {

            Throwable exception =
                    createUserTask.getException();

            String message = exception.getMessage();

            if (message == null || message.isBlank()) {
                message = "Unable to create user.";
            }

            showMessage(message, false);
        });

        Thread thread = new Thread(createUserTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void clearForm(){
        fullNameField.clear();
        usernameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();

        fullNameField.requestFocus();
    }


// taken help!! don't know the syntax
    private void showMessage(String message, boolean success) {

        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        messageLabel.getStyleClass().removeAll(
                "error-message",
                "success-message"
        );

        messageLabel.getStyleClass().add(
                success
                        ? "success-message"
                        : "error-message"
        );

        messageTimer.playFromStart();
    }
    @FXML
    private void handleCancel(){
        AdminNavigator.goTo("users");
    }



}
