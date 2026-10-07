package com.example.seal.controller.teacher;
import com.example.seal.model.Exam;
import com.example.seal.service.*;
import com.example.seal.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class ResultsController {
    @FXML private VBox root, answers;
    @FXML private ComboBox<Exam> exams;
    @FXML private TableView<JsonNode> table;
    @FXML private TableColumn<JsonNode,String> studentColumn, statusColumn, scoreColumn;
    @FXML private Label message;
    @FXML private Button save;
    private final TeacherExamService service = new TeacherExamService();
    private final Map<Long, Spinner<Integer>> marks = new LinkedHashMap<>();
    private long selectedSubmission;
    @FXML private void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        studentColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().path("studentName").asText()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().path("status").asText()));
        scoreColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().path("totalScore").isNull() ? "-" :
                c.getValue().path("totalScore").asText() + " / " + c.getValue().path("maximumScore").asText()));
        table.setPlaceholder(new Label("No attempts for this exam."));
        exams.valueProperty().addListener((o,a,b) -> refresh());
        table.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> show(b));
        save.setDisable(true);
        javafx.application.Platform.runLater(() -> {
            String token = UserSession.getToken();
            FxRequest.run(root, message, () -> service.list(token), rows -> {
                exams.getItems().setAll(rows);
                if (!rows.isEmpty()) exams.getSelectionModel().selectFirst();
            });
        });
    }
    @FXML private void refresh() {
        Exam exam = exams.getValue();
        if (exam == null) return;
        String token = UserSession.getToken();
        FxRequest.run(root, message, () -> service.submissions(exam.getId(), token), data -> {
            table.getItems().clear(); data.forEach(table.getItems()::add); answers.getChildren().clear(); marks.clear(); save.setDisable(true);
        });
    }
    private void show(JsonNode row) {
        answers.getChildren().clear(); marks.clear(); save.setDisable(true);
        if (row == null || exams.getValue() == null) return;
        Exam exam = exams.getValue(); selectedSubmission = row.get("id").asLong();
        long id = selectedSubmission; String token = UserSession.getToken();
        FxRequest.run(root, message, () -> service.detail(exam.getId(), id, token), data -> {
            for (JsonNode item : data.get("answers")) {
                Label question = new Label(item.get("questionText").asText() + " (" + item.get("marks").asInt() + " marks)");
                question.setWrapText(true); question.setStyle("-fx-font-weight: bold;");
                answers.getChildren().add(question);
                if ("TEXT".equals(item.get("type").asText())) {
                    Label response = new Label("Answer: " + item.path("textAnswer").asText("")); response.setWrapText(true);
                    Label reference = new Label("Marking guide: " + item.path("referenceAnswer").asText("")); reference.setWrapText(true);
                    int maximum = item.get("marks").asInt();
                    Spinner<Integer> score = new Spinner<>(0, maximum, item.path("awardedMarks").asInt(0));
                    score.setAccessibleText("Awarded marks for " + item.get("questionText").asText());
                    marks.put(item.get("questionId").asLong(), score);
                    answers.getChildren().addAll(response, reference, new HBox(10, new Label("Awarded marks"), score));
                } else {
                    int index = item.path("selectedOptionIndex").asInt(-1);
                    String chosen = index < 0 ? "Unanswered" : item.get("options").get(index).asText();
                    Label response = new Label("Selected: " + chosen + " | Marks: " + item.path("awardedMarks").asInt());
                    response.setWrapText(true); answers.getChildren().add(response);
                }
                answers.getChildren().add(new Separator());
            }
            save.setDisable(marks.isEmpty() || "IN_PROGRESS".equals(data.get("result").get("status").asText()));
        });
    }
    @FXML private void grade() {
        if (marks.isEmpty() || exams.getValue() == null) return;
        List<Map<String,Object>> grades = new ArrayList<>();
        marks.forEach((id, spinner) -> grades.add(Map.of("questionId", id, "awardedMarks", spinner.getValue())));
        Long examId = exams.getValue().getId(); long id = selectedSubmission; String token = UserSession.getToken();
        FxRequest.run(root, message, () -> service.grade(examId, id, grades, token), data -> refresh());
    }
}
