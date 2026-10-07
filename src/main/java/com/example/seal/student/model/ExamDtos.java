package com.example.seal.student.model;

import java.time.Instant;
import java.util.List;

/** Client contracts; confirm wire fields before REST integration. */
public final class ExamDtos {
    private ExamDtos() { }
    public enum QuestionType { MCQ, TEXT }
    public record AccessRequest(String registrationNumber, String symbolNumber, String accessCode) { }
    public record ExamAccess(String accessId, String title, String instructions,
                             int durationMinutes, int questionCount) { }
    public record Option(String id, String text) { }
    public record Question(String id, QuestionType type, String text, int marks, List<Option> options) {
        public Question { options = List.copyOf(options); }
    }
    public record Attempt(String id, String title, Instant expiresAt, List<Question> questions) {
        public Attempt { questions = List.copyOf(questions); }
    }
    public record Answer(String questionId, String selectedOptionId, String textAnswer) { }
    public record Submission(String attemptId, String requestId, List<Answer> answers) {
        public Submission { answers = List.copyOf(answers); }
    }
    public record Receipt(String reference, String message) { }
}
