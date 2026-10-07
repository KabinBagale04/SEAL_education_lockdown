package com.example.seal.controller.student;
import com.example.seal.service.*;
import com.example.seal.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class StudentResultsController {
    @FXML private VBox root;
    @FXML private Label message;
    @FXML private TableView<JsonNode> table;
    @FXML private TableColumn<JsonNode,String> titleColumn, statusColumn, scoreColumn;
    @FXML private void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        titleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().path("examTitle").asText()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty("GRADED".equals(c.getValue().path("status").asText()) ? "Graded" : "Awaiting manual grading"));
        scoreColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().path("totalScore").asText() + " / "
                + c.getValue().path("maximumScore").asText() + ("GRADED".equals(c.getValue().path("status").asText()) ? "" : " (partial)")));
        table.setPlaceholder(new Label("No submitted examinations."));
        javafx.application.Platform.runLater(this::refresh);
    }
    @FXML private void refresh() {
        String token = UserSession.getToken();
        FxRequest.run(root, message, () -> new ApiClient().request("GET", "/api/student/exams/results", null, token),
                rows -> { table.getItems().clear(); rows.forEach(table.getItems()::add); });
    }
}
