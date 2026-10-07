package com.example.seal.controller.teacher;
import com.example.seal.model.*;
import com.example.seal.navigation.TeacherNavigator;
import com.example.seal.service.*;
import com.example.seal.session.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ExamsController {
    @FXML private VBox root;
    @FXML private TableView<Exam> table;
    @FXML private TableColumn<Exam,String> titleColumn, subjectColumn, statusColumn, codeColumn;
    @FXML private Label message;
    @FXML private Button editButton, publishButton, closeButton;
    private final TeacherExamService service = new TeacherExamService();
    @FXML private void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        titleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        subjectColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubjectCode()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        codeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAccessCode()));
        table.setPlaceholder(new Label("No examinations."));
        table.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> selection());
        selection(); javafx.application.Platform.runLater(this::refresh);
    }
    private void selection() {
        Exam exam = table.getSelectionModel().getSelectedItem();
        editButton.setDisable(exam == null || exam.getStatus() != ExamStatus.DRAFT);
        publishButton.setDisable(exam == null || exam.getStatus() != ExamStatus.DRAFT);
        closeButton.setDisable(exam == null || exam.getStatus() != ExamStatus.PUBLISHED);
    }
    @FXML private void refresh() {
        String token = UserSession.getToken();
        FxRequest.run(root, message, () -> service.list(token), exams -> table.getItems().setAll(exams));
    }
    @FXML private void create() { TeacherWorkspace.editing = null; TeacherNavigator.goTo("create-exam"); }
    @FXML private void edit() {
        TeacherWorkspace.editing = table.getSelectionModel().getSelectedItem();
        if (TeacherWorkspace.editing != null) TeacherNavigator.goTo("create-exam");
    }
    @FXML private void publish() { change("publish"); }
    @FXML private void close() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Close this exam? Students will no longer be able to access or submit it.", ButtonType.OK, ButtonType.CANCEL);
        alert.initOwner(root.getScene().getWindow());
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) change("close");
    }
    private void change(String action) {
        Exam exam = table.getSelectionModel().getSelectedItem();
        if (exam == null) return;
        String token = UserSession.getToken();
        FxRequest.run(root, message, () -> service.change(exam.getId(), action, token), updated -> refresh());
    }
}
