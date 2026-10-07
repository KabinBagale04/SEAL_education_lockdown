package com.example.seal.controller.student;
import com.example.seal.session.UserSession;
import com.example.seal.student.model.ExamDtos.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.time.Instant;

public final class ExaminationController extends StudentPage {
    @FXML private Label title, timer, progress, questionTitle, questionText, confirmationText;
    @FXML private FlowPane navigation;
    @FXML private VBox answerArea, confirmation;
    @FXML private Button previous, next, submit, confirm, cancel;
    @FXML private BorderPane root;
    private Timeline clock;
    private int index;
    private boolean expired;
    private Submission pendingSubmission;
    @FXML private void initialize() {
        setup();
        if (flow.attempt == null) throw new IllegalStateException("Start an exam before opening this page.");
        title.setText(flow.attempt.title());
        showQuestion();
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        clock.setCycleCount(Timeline.INDEFINITE);
        root.sceneProperty().addListener((o, oldScene, scene) -> { if (scene == null) clock.stop(); });
        clock.play();
        javafx.application.Platform.runLater(this::tick);
    }
    private void tick() {
        if (flow.receipt != null) { clock.stop(); return; }
        long seconds = Math.max(0, (java.time.Duration.between(Instant.now(), flow.attempt.expiresAt()).toMillis() + 999) / 1000);
        timer.setText(String.format("%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60));
        if (seconds == 0 && !expired) {
            expired = true; clock.stop(); setConfirmation(false); applyDisabled();
            if (!busy) send();
        }
    }
    private void showQuestion() {
        Question question = flow.attempt.questions().get(index);
        questionTitle.setText("Question " + (index + 1) + " of " + flow.attempt.questions().size() + "  |  " + question.marks() + " marks");
        questionText.setText(question.text());
        answerArea.getChildren().clear();
        Answer saved = flow.answers.get(question.id());
        if (question.type() == QuestionType.MCQ) {
            ToggleGroup group = new ToggleGroup();
            for (Option option : question.options()) {
                RadioButton choice = new RadioButton(option.text());
                choice.setWrapText(true); choice.setMaxWidth(Double.MAX_VALUE); choice.setToggleGroup(group);
                choice.setSelected(saved != null && option.id().equals(saved.selectedOptionId()));
                choice.setOnAction(e -> {
                    flow.answers.put(question.id(), new Answer(question.id(), option.id(), "")); refreshNavigation();
                });
                answerArea.getChildren().add(choice);
            }
        } else {
            TextArea text = new TextArea(saved == null ? "" : saved.textAnswer());
            text.setWrapText(true); text.setPrefRowCount(8);
            text.setAccessibleText("Answer for question " + (index + 1));
            text.textProperty().addListener((o, oldValue, value) -> {
                flow.answers.put(question.id(), new Answer(question.id(), null, value)); refreshNavigation();
            });
            answerArea.getChildren().add(text);
        }
        refreshNavigation(); applyDisabled();
    }
    private void refreshNavigation() {
        progress.setText(flow.answeredCount() + " of " + flow.attempt.questions().size() + " answered");
        navigation.getChildren().clear();
        for (int i = 0; i < flow.attempt.questions().size(); i++) {
            final int target = i;
            Answer answer = flow.answers.get(flow.attempt.questions().get(i).id());
            boolean answered = answer != null && (answer.selectedOptionId() != null || !answer.textAnswer().isBlank());
            Button button = new Button(Integer.toString(i + 1));
            button.getStyleClass().add("question-number");
            if (answered) button.getStyleClass().add("answered");
            if (i == index) button.getStyleClass().add("current");
            button.setAccessibleText("Question " + (i + 1) + (answered ? ", answered" : ", unanswered") + (i == index ? ", current" : ""));
            button.setOnAction(e -> { index = target; showQuestion(); });
            navigation.getChildren().add(button);
        }
    }
    private void applyDisabled() {
        boolean locked = busy || expired || pendingSubmission != null || confirmation.isVisible();
        answerArea.setDisable(locked); navigation.setDisable(locked);
        previous.setDisable(locked || index == 0);
        next.setDisable(locked || index == flow.attempt.questions().size() - 1);
        submit.setDisable(busy || confirmation.isVisible());
        submit.setText(busy ? "Submitting..." : pendingSubmission != null ? "Retry submission" : "Submit exam");
        confirm.setDisable(busy); cancel.setDisable(busy);
    }
    @FXML private void previous() { if (index > 0) { index--; showQuestion(); } }
    @FXML private void next() { if (index + 1 < flow.attempt.questions().size()) { index++; showQuestion(); } }
    @FXML private void requestSubmit() {
        if (pendingSubmission != null || expired) { send(); return; }
        long unanswered = flow.attempt.questions().size() - flow.answeredCount();
        confirmationText.setText("Submit this examination? " + unanswered + " question(s) unanswered. Answers cannot be edited after submission.");
        setConfirmation(true);
    }
    private void setConfirmation(boolean visible) {
        confirmation.setVisible(visible); confirmation.setManaged(visible); applyDisabled();
    }
    @FXML private void cancelSubmit() { setConfirmation(false); }
    @FXML private void confirmSubmit() { setConfirmation(false); send(); }
    private void send() {
        if (busy) return;
        // Freeze payload and request ID for retries after an ambiguous network failure.
        if (pendingSubmission == null) pendingSubmission = flow.submission();
        String token = UserSession.getToken();
        run(() -> flow.service.submit(pendingSubmission, token), receipt -> {
            if (receipt == null || receipt.reference() == null || receipt.reference().isBlank()) {
                message.setText("No submission acknowledgement received. Please retry."); return;
            }
            clock.stop(); flow.receipt = receipt; flow.go("acknowledgement");
        }, value -> applyDisabled());
    }
}
