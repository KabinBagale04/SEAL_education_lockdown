package com.example.seal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource(
                       // "/com/example/seal/login.fxml"//  use this to transfer control to login page
                       "/com/example/seal/teacher/teacher-shell.fxml"
                )
        );

        Scene scene = new Scene(loader.load(),1100,700);
        //uses the class navigator .java so that student and teacher ko dashboard can be differentiated
        Navigator.setScene(scene);

        stage.setTitle("SEAL - Secured Examination Access and Lockdown");
        stage.setMinHeight(600);
        stage.setMinWidth(900);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}