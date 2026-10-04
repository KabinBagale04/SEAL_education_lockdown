package com.example.seal.controller.teacher;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javax.swing.*;
import com.example.seal.model.Question;
import com.example.seal.model.QuestionType;
import java.util.List;

public class QuestionEditorController {
    private VBox mcqEditor;
    private VBox textEditor;

    private TextField optionAField;
    private TextField optionBField;
    private TextField optionCField;
    private TextField optionDField;

    private  ToggleGroup correctAnswerGroup;

    private RadioButton optionARadio;
    private RadioButton optionBRadio;
    private RadioButton optionCRadio;
    private RadioButton optionDRadio;

    private TextArea referenceAnswerArea;

    @FXML
    private Label questionNumberLabel;
    @FXML
    private ComboBox<String> questionTypeBox;
    @FXML
    private VBox typeSpecificArea;

    @FXML
    private void handleDelete(){
        if(onDelete!=null){
            onDelete.run();
        }
    }

    @FXML
    private TextArea questionTextArea;

    @FXML
    private TextField marksField;

    @FXML
    private void initialize(){
        questionTypeBox.setItems(
                FXCollections.observableArrayList(
                        "Multiple Choice",
                        "Text Answers"
                )
        );

        createMcqEditor();
        createTextEditor();

        questionTypeBox.setValue("Multiple Choice");
        showMcqEditor();

        questionTypeBox.valueProperty().addListener(
                ((observable, oldValue, newValue) -> {
                    if("Multiple Choice".equals(newValue)){
                        showMcqEditor();
                    }else {
                        showTextEditor();
                    }
                })
        );
        marksField.setTextFormatter(
                new TextFormatter<String>(
                        change->{
                            String newText = change.getControlNewText();

                            if(newText.matches("\\d{0,2}")){
                                return change;
                            }
                            return null;
                        }
                )
        );
    }

    private void showMcqEditor(){
        typeSpecificArea.getChildren().clear();

        typeSpecificArea.getChildren().addAll(mcqEditor);
    }

    private void createMcqEditor(){
        mcqEditor = new VBox(10);
        Label optionsLabel = new Label("Options- select the correct answer");
        optionsLabel.getStyleClass().add("form-label");

        correctAnswerGroup = new ToggleGroup();

        optionARadio = new RadioButton("A");
        optionBRadio = new RadioButton("B");
        optionCRadio = new RadioButton("C");
        optionDRadio = new RadioButton("D");

        optionAField = createOptionField("Enter option A");
        HBox optionARow = createOptionRow(optionARadio,optionAField);

        optionBRadio = new RadioButton("B");
        optionBRadio.setToggleGroup(correctAnswerGroup);

        optionBField = createOptionField("Enter option B");

        HBox optionBRow =
                createOptionRow(optionBRadio, optionBField);


        optionCRadio = new RadioButton("C");
        optionCRadio.setToggleGroup(correctAnswerGroup);

        optionCField = createOptionField("Enter option C");

        HBox optionCRow =
                createOptionRow(optionCRadio, optionCField);


        optionDRadio = new RadioButton("D");
        optionDRadio.setToggleGroup(correctAnswerGroup);

        optionDField = createOptionField("Enter option D");

        HBox optionDRow =
                createOptionRow(optionDRadio, optionDField);


        mcqEditor.getChildren().addAll(
                optionsLabel,
                optionARow,
                optionBRow,
                optionCRow,
                optionDRow
        );

    }

    private TextField createOptionField(String prompt){
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-field");
        HBox.setHgrow(field,Priority.ALWAYS);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }
    private HBox createOptionRow(RadioButton radio, TextField field){
        HBox row = new HBox(10,radio,field);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
    private void createTextEditor(){
        textEditor = new VBox(8);

        Label answerLabel =
                new Label("Reference Answer / Marking Guide");

        answerLabel.getStyleClass().add("form-label");


        referenceAnswerArea = new TextArea();

        referenceAnswerArea.setPromptText(
                "Enter the expected answer or guidance for manual grading..."
        );

        referenceAnswerArea.setWrapText(true);
        referenceAnswerArea.setPrefRowCount(4);

        referenceAnswerArea
                .getStyleClass()
                .add("form-text-area");


        Label hint = new Label(
                "Text answers will be reviewed manually by the teacher."
        );

        hint.getStyleClass().add("field-hint");


        textEditor.getChildren().addAll(
                answerLabel,
                referenceAnswerArea,
                hint
        );
    }

    private void showTextEditor(){
        typeSpecificArea.getChildren().setAll(textEditor);
    }

    public void setQuestionNumber(int number){
        questionNumberLabel.setText("Question "+number);
    }

    private Runnable onDelete;

    public void setOnDelete(Runnable onDelete){
        this.onDelete = onDelete;
    }

    public Question getQuestion(){
        Question question = new Question();

        question.setQuestionText(
                questionTextArea.getText().trim()
        );

        int marks = Integer.parseInt(marksField.getText());

        question.setMarks(marks);

        if("Multiple Choice".equals(
                questionTypeBox.getValue()
        )){
            question.setType(QuestionType.MCQ);
            question.setOptions(
                    List.of(
                            optionAField.getText().trim(),
                            optionBField.getText().trim(),
                            optionCField.getText().trim(),
                            optionDField.getText().trim()
                    )
            );
            question.setCorrectOption(getSelectedCorrectOption());
        }else {
            question.setType(QuestionType.TEXT);
            question.setReferenceAnswer(
                    referenceAnswerArea.getText().trim()
            );
        }
        return question;
    }

    private int getSelectedCorrectOption(){
        if(optionARadio.isSelected()){
            return 0;
        }if(optionBRadio.isSelected()){
            return 1;
        }if(optionCRadio.isSelected()){
            return 2;
        }
        return 3;
    }

    //validate question ma sabai metadata correct xa or not
    public String validateQuestion() {

        if (questionTextArea.getText().trim().isEmpty()) {
            return "Question text cannot be empty.";
        }

        if (marksField.getText().isBlank()) {
            return "Marks cannot be empty.";
        }

        int marks = Integer.parseInt(
                marksField.getText()
        );

        if (marks <= 0) {
            return "Marks must be greater than 0.";
        }


        if ("Multiple Choice".equals(
                questionTypeBox.getValue())) {

            if (optionAField.getText().trim().isEmpty()
                    || optionBField.getText().trim().isEmpty()
                    || optionCField.getText().trim().isEmpty()
                    || optionDField.getText().trim().isEmpty()) {

                return "All four MCQ options are required.";
            }

            if (correctAnswerGroup
                    .getSelectedToggle() == null) {

                return "Select the correct MCQ answer.";
            }

        } else {

            if (referenceAnswerArea
                    .getText()
                    .trim()
                    .isEmpty()) {

                return "Provide a reference answer for the text question.";
            }
        }

        return null;
    }
}
