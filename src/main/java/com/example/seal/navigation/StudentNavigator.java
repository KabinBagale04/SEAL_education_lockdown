package com.example.seal.navigation;

import com.example.seal.Navigator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.function.Consumer;

public final class StudentNavigator {
    private static StackPane contentArea;
    private static Consumer<String> selection;

    private StudentNavigator() { }

    public static void attach(StackPane area, Consumer<String> onSelection) {
        contentArea = area;
        selection = onSelection;
    }

    public static void goTo(String page) {
        if (!java.util.Set.of("dashboard", "access", "results").contains(page))
            throw new IllegalArgumentException("Unknown student page: " + page);
        try {
            Parent content = FXMLLoader.load(StudentNavigator.class.getResource(
                    "/com/example/seal/student/" + page + ".fxml"));
            contentArea.getChildren().setAll(content);
            selection.accept(page);
        } catch (IOException error) {
            throw new IllegalStateException("Could not load student page: " + page, error);
        }
    }

    /** Return from the full-screen exam to a fresh workspace in the same Scene. */
    public static void open(String page) {
        Navigator.goTo("student-home");
        if (!"dashboard".equals(page)) goTo(page);
    }
}
