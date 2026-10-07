package com.example.seal.model;
import java.util.List;

public class Question {
    private QuestionType type;
    private String questionText;
    private int marks;

    private List<String> options;
    @com.fasterxml.jackson.annotation.JsonAlias("correctOptionIndex")
    private Integer correctOption;

    private String referenceAnswer;

    public QuestionType getType(){
        return type;
    }
    public void setType(QuestionType type){
        this.type =type;
    }
    public String getQuestionText(){
        return questionText;
    }
    public void setQuestionText(String questionText){
        this.questionText=questionText;
    }
    public int getMarks(){
        return marks;
    }
    public void setMarks(int marks){
        this.marks=marks;
    }
    public List<String> getOptions(){
        return options;
    }
    public void setOptions(List<String> options){
        this.options=options;
    }
    public Integer getCorrectOption(){
        return correctOption;
    }
    public void setCorrectOption(Integer correctOption){
        this.correctOption=correctOption;
    }
    public String getReferenceAnswer(){
        return referenceAnswer;
    }
    public void setReferenceAnswer(String referenceAnswer){
        this.referenceAnswer=referenceAnswer;
    }
}
