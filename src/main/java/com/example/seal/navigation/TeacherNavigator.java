package com.example.seal.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;


import java.io.IOException;

public class TeacherNavigator {
    private static StackPane contentArea;

    public static void setContentArea(StackPane contentArea){
        TeacherNavigator.contentArea = contentArea;
    }
    public static void goTo(String page){
        try{
            Parent content = FXMLLoader.load(TeacherNavigator.class.getResource("/com/example/seal/teacher/"+page+".fxml"));
            contentArea.getChildren().setAll(content);
        }catch (IOException e){
            throw new RuntimeException("Could not load teacher page: "+page,e);
        }
    }
}
