package com.example.seal.controller.student;
import com.example.seal.session.UserSession;
import com.example.seal.student.StudentFlow;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public abstract class StudentPage {
    @FXML protected Label message;
    @FXML protected Label mode;
    protected StudentFlow flow;
    protected boolean busy;
    protected void setup() {
        flow = StudentFlow.current();
        mode.setText(flow.service.isDemo() ? "DEMO - local only" : "Student examination");
        message.setText("");
    }
    protected <T> void run(Callable<T> action, Consumer<T> success, Consumer<Boolean> setBusy) {
        if (busy) return;
        busy = true;
        setBusy.accept(true);
        message.setText("");
        String token = UserSession.getToken();
        Task<T> task = new Task<>() { protected T call() throws Exception { return action.call(); } };
        task.setOnSucceeded(event -> {
            busy = false; setBusy.accept(false);
            if (Objects.equals(token, UserSession.getToken())) success.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            busy = false; setBusy.accept(false);
            if (!Objects.equals(token, UserSession.getToken())) return;
            Throwable error = task.getException();
            message.setText(error instanceof IllegalArgumentException || error instanceof IllegalStateException
                    ? error.getMessage() : "Unable to reach the exam service. Your answers are retained; please retry.");
        });
        Thread thread = new Thread(task, "student-exam-request");
        thread.setDaemon(true);
        thread.start();
    }
}
