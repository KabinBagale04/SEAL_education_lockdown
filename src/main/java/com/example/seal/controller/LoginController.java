package com.example.seal.controller;
import com.example.seal.dto.LoginResponse;
import com.example.seal.service.AuthService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import com.example.seal.Navigator;
import com.example.seal.session.UserSession;

public class LoginController {
    private final AuthService authService = new AuthService();
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;


    @FXML
    private Label messageLabel;

    @FXML
    private void handleClear(){
        clearField();
    }
    private void clearField(){
        usernameField.clear();
        passwordField.clear();
        clearError();
        usernameField.requestFocus();
    }
    @FXML
    private void initialize(){
        // this is a property useage, so yo ma chai we use java pbjects and their properties to access few records to get pre-built functions
        usernameField.textProperty().addListener(
                (observable ,oldValue, newValue)-> clearError()
        );
        passwordField.textProperty().addListener(
                (observable,oldValue,newValue)->clearError()
        );
    }
    @FXML
    private void handleLogin(){
        clearError();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();


        if(username.isEmpty() || password.isEmpty()){
            showError("Please enter your username and password");
            return;
        }

        // taks<>() creates a background object, so our UI wont freeze and handles this authentication from the background
        Task<LoginResponse> loginTask = new Task<>(){
            @Override
            protected LoginResponse call() throws Exception{
                return authService.authenticate(username,password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            LoginResponse response = loginTask.getValue();

            if(!response.success()){
                showError(response.message());
                return;
            }

            UserSession.start(
                    response.userId(),
                    response.fullName(),
                    response.role(),
                    response.token()
            );

            switch (response.role()){
                case "TEACHER" -> Navigator.goTo("teacher/teacher-shell");
                case "STUDENT" -> Navigator.goTo("student-home");
                case "ADMIN" -> Navigator.goTo("admin/admin-shell");
                default -> showError("Unknown user role");
            }
        });

        loginTask.setOnFailed(event -> {
            Throwable error = loginTask.getException();
            showError(error instanceof IllegalStateException
                    ? error.getMessage()
                    : "Unable to connect to the SEAL server. " + error.getClass().getSimpleName());

        });
        Thread thread = new Thread(loginTask);
        // setDaemon means background thread should not prevent application from exiting if the jacafx application closes
        thread.setDaemon(true);
        thread.start();
    }

    private void showError(String message){
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    private void clearError(){
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
}
