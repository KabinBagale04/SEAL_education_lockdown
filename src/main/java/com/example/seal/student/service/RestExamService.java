package com.example.seal.student.service;

import com.example.seal.service.ApiClient;
import com.example.seal.student.model.ExamDtos.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.*;

public final class RestExamService implements ExamService {
    private final ApiClient api = new ApiClient();
    private final Map<String, JsonNode> accessed = new HashMap<>();
    private final Map<String, String> attemptExams = new HashMap<>();
    public ExamAccess validateAccess(AccessRequest request, String token) throws Exception {
        JsonNode exam = api.request("POST", "/api/student/exams/access", request, token);
        String id = exam.get("id").asText();
        accessed.clear(); attemptExams.clear(); accessed.put(id, exam);
        return new ExamAccess(id, exam.get("title").asText(), exam.path("instructions").asText(),
                exam.get("durationMinutes").asInt(), exam.get("questions").size());
    }
    public Attempt startExam(String accessId, String requestId, String token) throws Exception {
        JsonNode exam = accessed.get(accessId);
        if (exam == null) throw new IllegalStateException("Validate exam access first.");
        JsonNode start = api.request("POST", "/api/student/exams/" + accessId + "/start", null, token);
        List<Question> questions = new ArrayList<>();
        for (JsonNode q : exam.get("questions")) {
            List<Option> options = new ArrayList<>();
            for (int i = 0; i < q.get("options").size(); i++) options.add(new Option(Integer.toString(i), q.get("options").get(i).asText()));
            questions.add(new Question(q.get("id").asText(), QuestionType.valueOf(q.get("type").asText()),
                    q.get("questionText").asText(), q.get("marks").asInt(), options));
        }
        String attemptId = start.get("submissionId").asText();
        attemptExams.put(attemptId, accessId);
        return new Attempt(attemptId, exam.get("title").asText(), Instant.parse(start.get("expiresAt").asText()), questions);
    }
    public Receipt submit(Submission submission, String token) throws Exception {
        String examId = attemptExams.get(submission.attemptId());
        if (examId == null) throw new IllegalStateException("Attempt is not available in this session.");
        List<Map<String, Object>> answers = new ArrayList<>();
        for (Answer answer : submission.answers()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("questionId", Long.valueOf(answer.questionId()));
            data.put("selectedOptionIndex", answer.selectedOptionId() == null ? null : Integer.valueOf(answer.selectedOptionId()));
            data.put("textAnswer", answer.textAnswer()); answers.add(data);
        }
        JsonNode receipt = api.request("POST", "/api/student/exams/" + examId + "/submit", answers, token);
        return new Receipt(receipt.get("id").asText(), receipt.get("message").asText() + " Score: "
                + receipt.get("totalScore").asInt() + (receipt.get("pendingManualGrading").asBoolean() ? " (MCQ subtotal)." : "."));
    }
}
