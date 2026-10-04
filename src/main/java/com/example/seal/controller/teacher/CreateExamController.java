package com.example.seal.controller.teacher;

import com.example.seal.model.Exam;
import com.example.seal.model.ExamStatus;
import com.example.seal.model.Question;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateExamController {
    @FXML
    private VBox questionsContainer;
    @FXML
    private TextField titleField;
    @FXML
    private TextField subjectField;
    @FXML
    private TextField subjectCodeField;
    @FXML
    private TextField durationField;
    @FXML
    private TextArea instructionsArea;
    @FXML
    private Label formMessageLabel;

    @FXML
    private VBox emptyQuestionsState;

    private final List<QuestionEntry> questions = new ArrayList<>();
    private static class QuestionEntry{
        private final Parent view;
        private final QuestionEditorController controller;

        public QuestionEntry(Parent view, QuestionEditorController controller){
            this.view = view;
            this.controller=controller;
        }

    }
    @FXML
    private void initialize(){
        durationField.setTextFormatter(
                new TextFormatter<String>(
                        change->{
                            String newText = change.getControlNewText();

                            if(newText.matches("\\d{0,3}")){
                                return change;
                            }
                            return null;
                        }
                )
        );
        updateEmptyState();
    }
    @FXML
    private void handleAddQuestion(){
        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/seal/teacher/question-editor.fxml")
            );

            Parent view = loader.load();

            QuestionEditorController controller = loader.getController();
            QuestionEntry entry = new QuestionEntry(view,controller);
            questions.add(entry);
            controller.setQuestionNumber(questions.size());

            controller.setOnDelete(
                    ()->deleteQuestion(entry)
            );
            questionsContainer.getChildren().add(view);
            updateEmptyState();
        }catch (IOException e){
            throw new RuntimeException("Couldn't load question editor.",e);
        }
    }

    @FXML
    private void handleSaveDraft(){
        Exam exam = buildExam(ExamStatus.DRAFT);
        if (exam != null) {
            printExam(exam);
        }
    }
    @FXML
    private void handlePublish(){
        if(!validateExam()){
            return;
        }

        Exam exam = buildExam(ExamStatus.PUBLISHED);
        if (exam != null) {
            printExam(exam);
        }
    }

    private void printExam(Exam exam) {

        System.out.println("-------------------------");
        System.out.println("EXAM");
        System.out.println("-------------------------");

        System.out.println(
                "Title: " + exam.getTitle()
        );

        System.out.println(
                "Subject: " + exam.getSubject()
        );

        System.out.println(
                "Subject Code: " + exam.getSubjectCode()
        );

        System.out.println(
                "Duration: "
                        + exam.getDurationMinutes()
                        + " minutes"
        );

        System.out.println(
                "Status: " + exam.getStatus()
        );

        System.out.println(
                "Questions: "
                        + exam.getQuestions().size()
        );


        for (int i = 0;
             i < exam.getQuestions().size();
             i++) {

            Question question =
                    exam.getQuestions().get(i);

            System.out.println(
                    (i + 1)
                            + ". "
                            + question.getType()
                            + " | "
                            + question.getQuestionText()
                            + " | "
                            + question.getMarks()
                            + " marks"
            );
        }
    }
    private void deleteQuestion(QuestionEntry entry){
        questions.remove(entry);
        questionsContainer.getChildren().remove(entry.view);
        renumberQuestions();
        updateEmptyState();
    }

    private void renumberQuestions(){
        for(int i = 0 ; i < questions.size();i++){
            questions.get(i).controller.setQuestionNumber(i+1);
        }
    }

    private void updateEmptyState(){
        boolean empty = questions.isEmpty();
        emptyQuestionsState.setVisible(empty);
        emptyQuestionsState.setManaged(empty);
    }
    private Exam buildExam(ExamStatus status){
        Integer durationMinutes = readDurationMinutes();
        if (durationMinutes == null) {
            return null;
        }

        Exam exam = new Exam();

        exam.setTitle(
                titleField.getText().trim()
        );
        exam.setSubject(
                subjectField.getText().trim()
        );
        exam.setSubjectCode(
                subjectCodeField.getText().trim()
        );
        exam.setDurationMinutes(durationMinutes);
        exam.setInstructions(
                instructionsArea.getText().trim()
        );
        exam.setStatus(status);

        List<Question> examQuestion = new ArrayList<>();

        for (QuestionEntry entry:questions){
            Question question = entry.controller.getQuestion();
            examQuestion.add(question);
        }
        exam.setQuestions(examQuestion);
        return exam;
    }

    private Integer readDurationMinutes() {
        String durationText = durationField.getText().trim();

        if (durationText.isEmpty()) {
            showValidationError("Enter the exam duration in minutes.");
            return null;
        }

        int durationMinutes = Integer.parseInt(durationText);
        if (durationMinutes <= 0) {
            showValidationError("Exam duration must be greater than zero.");
            return null;
        }

        return durationMinutes;
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid exam details");
        alert.setHeaderText("Please check the exam duration");
        alert.setContentText(message);
        alert.showAndWait();
    }

    //error message to the bottom during publish
    private void showError(String message){
        formMessageLabel.setText(message);
        formMessageLabel.setVisible(true);
        formMessageLabel.setManaged(true);
    }
    private void clearError(){
        formMessageLabel.setText("");
        formMessageLabel.setVisible(false);
        formMessageLabel.setManaged(false);
    }

    //now this is for validation, mathi ko two helps to show and clear error during validation
    private boolean validateExam(){
        clearError();
        if(titleField.getText().trim().isEmpty()){
            showError("Please enter an examination title");
            titleField.requestFocus();
            return false;
        }

        if (subjectField.getText().trim().isEmpty()) {
            showError("Please enter the subject.");
            subjectField.requestFocus();
            return false;
        }


        if (subjectCodeField.getText().trim().isEmpty()) {
            showError("Please enter the subject code.");
            subjectCodeField.requestFocus();
            return false;
        }


        if (durationField.getText().isBlank()) {
            showError("Please enter the examination duration.");
            durationField.requestFocus();
            return false;
        }


        int duration =
                Integer.parseInt(durationField.getText());

        if (duration <= 0) {
            showError("Examination duration must be greater than 0.");
            durationField.requestFocus();
            return false;
        }


        if (questions.isEmpty()) {
            showError("Add at least one question before publishing.");
            return false;
        }

        for (int i = 0; i < questions.size(); i++) {

            QuestionEditorController controller =
                    questions.get(i).controller;

            String error = controller.validateQuestion();

            if (error != null) {
                showError(
                        "Question " + (i + 1) + ": " + error
                );
                return false;
            }
        }
        return true;
    }


}
