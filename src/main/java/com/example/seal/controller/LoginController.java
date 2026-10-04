package com.example.seal.controller;
import com.example.seal.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Label;
import com.example.seal.Navigator;

public class LoginController {
    private final AuthService authService = new AuthService();
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private RadioButton studentRadio;
    @FXML
    private RadioButton teacherRadio;
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
        studentRadio.selectedProperty().addListener(
                (observable,oldValue,newValue)->{
                    if(newValue){
                        clearField();
                    }
                }
        );
        teacherRadio.selectedProperty().addListener(
                (observable,oldValue,newValue)->{
                    if(newValue){
                     clearField();
                    }
                }
        );
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

        String role;
        if(studentRadio.isSelected()){
            role = "STUDENT";
        }else{
            role = "TEACHER";
        }

        if(username.isEmpty() || password.isEmpty()){
            showError("Please enter your username and password");
            return;
        }
        boolean authenticated = authService.authenticate(username,password,role);
        if(authenticated){
            if(role.equals("STUDENT")){
                Navigator.goTo("student-home");
            }else{
                Navigator.goTo("teacher/teacher-shell");
            }
        }else{
           showError("Incorrect username and password");
        }
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
