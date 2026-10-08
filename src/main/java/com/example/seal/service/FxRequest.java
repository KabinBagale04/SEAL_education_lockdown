package com.example.seal.service;

import com.example.seal.session.UserSession;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class FxRequest {
    private FxRequest() { }
    public static <T> void run(Node view, Label message, Callable<T> work, Consumer<T> success) {
        if (view.isDisable()) return;
        String token = UserSession.getToken();
        view.setDisable(true);
        message.setManaged(true); message.setVisible(true); message.setText("Loading...");
        Task<T> task = new Task<>() { protected T call() throws Exception { return work.call(); } };
        task.setOnSucceeded(e -> {
            view.setDisable(false); message.setText("");
            if (Objects.equals(token, UserSession.getToken()) && view.getScene() != null) success.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            view.setDisable(false);
            if (Objects.equals(token, UserSession.getToken()))
                message.setText(errorMessage(task.getException()));
        });
        Thread thread = new Thread(task, "seal-client-request"); thread.setDaemon(true); thread.start();
    }

    private static String errorMessage(Throwable error) {
        if (error instanceof IllegalStateException) return error.getMessage();
        String detail = error.getMessage();
        if (detail == null || detail.isBlank()) detail = error.getClass().getSimpleName();
        return "Cannot complete server request: " + detail;
    }
}
