package com.example.seal.service;
import com.example.seal.Navigator;
import com.example.seal.session.UserSession;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

public final class SessionActions {
    private static boolean running;
    private SessionActions() { }
    public static void logout() {
        if (running) return;
        running = true;
        String token = UserSession.getToken();
        Task<Void> task = new Task<>() { protected Void call() throws Exception { new AuthService().logout(token); return null; } };
        Runnable done = () -> { running = false; UserSession.clear(); Navigator.goTo("login"); };
        task.setOnSucceeded(e -> done.run());
        task.setOnFailed(e -> {
            running = false;
            if (task.getException() instanceof IllegalStateException && task.getException().getMessage().startsWith("Session expired")) { done.run(); return; }
            new Alert(Alert.AlertType.ERROR, "Could not sign out on the server. Check the connection and try again.").showAndWait();
        });
        Thread thread = new Thread(task, "seal-logout"); thread.setDaemon(true); thread.start();
    }
}
