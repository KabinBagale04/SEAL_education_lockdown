package com.example.seal.controller.teacher;
import com.example.seal.model.*;
import com.example.seal.navigation.TeacherNavigator;
import com.example.seal.service.*;
import com.example.seal.session.UserSession;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.*;

public class CreateExamController {
    @FXML private ScrollPane root;
    @FXML private VBox questionsContainer, emptyQuestionsState;
    @FXML private TextField titleField, subjectField, subjectCodeField, durationField;
    @FXML private TextArea instructionsArea;
    @FXML private Label formMessageLabel;
    private final List<QuestionEditorController> questions = new ArrayList<>();
    private Long id;
    @FXML private void initialize() {
        durationField.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().matches("\\d{0,3}") ? c : null));
        Exam edit = TeacherWorkspace.editing;
        TeacherWorkspace.editing = null;
        if (edit != null) {
            id = edit.getId(); titleField.setText(edit.getTitle()); subjectField.setText(edit.getSubject());
            subjectCodeField.setText(edit.getSubjectCode()); durationField.setText(Integer.toString(edit.getDurationMinutes()));
            instructionsArea.setText(edit.getInstructions());
            for (Question q : edit.getQuestions()) add(q);
        }
        updateEmpty();
    }
    @FXML private void handleAddQuestion() { add(null); }
    private void add(Question initial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/seal/teacher/question-editor.fxml"));
            Parent view = loader.load();
            QuestionEditorController controller = loader.getController();
            questions.add(controller); questionsContainer.getChildren().add(view);
            if (initial != null) controller.setQuestion(initial);
            controller.setOnDelete(() -> { questions.remove(controller); questionsContainer.getChildren().remove(view); updateEmpty(); });
            updateEmpty();
        } catch (IOException e) { throw new IllegalStateException("Cannot load question editor.", e); }
    }
    private void updateEmpty() {
        emptyQuestionsState.setVisible(questions.isEmpty()); emptyQuestionsState.setManaged(questions.isEmpty());
        for (int i=0; i<questions.size(); i++) questions.get(i).setQuestionNumber(i+1);
    }
    @FXML private void handleSaveDraft() { save(ExamStatus.DRAFT); }
    @FXML private void handlePublish() { save(ExamStatus.PUBLISHED); }
    @FXML private void cancel() { TeacherNavigator.goTo("exams"); }
    private void error(String text) { formMessageLabel.setText(text); formMessageLabel.setVisible(true); formMessageLabel.setManaged(true); }
    private void save(ExamStatus status) {
        if (titleField.getText().isBlank() || subjectField.getText().isBlank() || subjectCodeField.getText().isBlank()) {
            error("Enter title, subject and subject code."); return;
        }
        int duration = durationField.getText().isBlank() ? 0 : Integer.parseInt(durationField.getText());
        if (duration < 1 || duration > 480) { error("Duration must be 1-480 minutes."); return; }
        if (status == ExamStatus.PUBLISHED && questions.isEmpty()) { error("Add a question before publishing."); return; }
        for (int i=0; i<questions.size(); i++) {
            String validation = questions.get(i).validateQuestion();
            if (validation != null) { error("Question " + (i+1) + ": " + validation); return; }
        }
        Exam exam = new Exam(); exam.setId(id); exam.setTitle(titleField.getText().trim());
        exam.setSubject(subjectField.getText().trim()); exam.setSubjectCode(subjectCodeField.getText().trim());
        exam.setDurationMinutes(duration); exam.setInstructions(instructionsArea.getText()); exam.setStatus(status);
        exam.setQuestions(questions.stream().map(QuestionEditorController::getQuestion).toList());
        String token = UserSession.getToken();
        FxRequest.run(root, formMessageLabel, () -> new TeacherExamService().save(exam, token), saved -> {
            id = saved.getId();
            TeacherNavigator.goTo("exams");
        });
    }
}
