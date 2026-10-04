package com.example.seal.model;

import java.util.ArrayList;
import java.util.List;

public class Exam {
    private String title;
    private String subject;
    private String subjectCode;

    private int durationMinutes;
    private String instructions;
    private ExamStatus status;

    private List<Question> questions = new ArrayList<>();

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getSubject(){
        return subject;
    }
    public void setSubject(String subject){
        this.subject = subject;
    }
    public String getSubjectCode(){
        return subjectCode;
    }
    public void setSubjectCode(String SubjectCode){
        this.subjectCode= SubjectCode;
    }
    public int getDurationMinutes(){
        return durationMinutes;
    }
    public void setDurationMinutes(int durationMinutes){
        this.durationMinutes = durationMinutes;
    }
    public String getInstructions(){
        return instructions;
    }
    public void setInstructions(String instructions){
        this.instructions = instructions;
    }
    public ExamStatus getStatus(){
        return status;
    }
    public void setStatus(ExamStatus status){
        this.status = status;
    }
    public void setQuestions(List<Question> questions){
        this.questions =questions;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
