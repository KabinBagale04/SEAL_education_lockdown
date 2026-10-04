package com.example.seal;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;


public class Navigator {
    private static Scene scene;
    public static void setScene(Scene scene){
        Navigator.scene = scene;
    }

    public static void goTo(String fxml){
        try{
            Parent root = FXMLLoader.load(
                    Navigator.class.getResource("/com/example/seal/"+fxml+".fxml")
            );
            scene.setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load page: "+fxml,e );
        }
    }
}
