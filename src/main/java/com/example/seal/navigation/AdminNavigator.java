package com.example.seal.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminNavigator {
    private static StackPane contentArea;

    public static void setContentArea(StackPane contentArea){
        AdminNavigator.contentArea = contentArea;
    }
    public static void goTo(String page){
        try{
            Parent content = FXMLLoader.load(
                    AdminNavigator.class.getResource(
                            "/com/example/seal/admin/"+page+".fxml"
                    )
            );
            contentArea.getChildren().setAll(content);
        }catch (IOException e){
            throw new RuntimeException("Could not load admin page: "+page,e);
        }
    }
}
