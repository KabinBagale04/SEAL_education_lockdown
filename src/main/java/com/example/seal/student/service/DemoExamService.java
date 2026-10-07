package com.example.seal.student.service;
import com.example.seal.student.model.ExamDtos.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Temporary fixtures; opt in with -Dseal.student.demo=true. */
public final class DemoExamService implements ExamService {
    private final Map<String, Attempt> starts = new ConcurrentHashMap<>();
    private final Map<String, Receipt> receipts = new ConcurrentHashMap<>();
    public boolean isDemo() { return true; }
    public ExamAccess validateAccess(AccessRequest request, String token) {
        if (!"DEMO".equals(request.accessCode())) throw new IllegalArgumentException("Demo access code: DEMO.");
        return new ExamAccess("demo-access", "SEAL practice examination",
                "Answer all questions. You may revisit questions before submitting. "
                + "When time expires, your current answers will be submitted. "
                + "This is a local demonstration; no answers are sent to the server.", 15, 3);
    }
    public Attempt startExam(String accessId, String requestId, String token) {
        if (!"demo-access".equals(accessId)) throw new IllegalArgumentException("Invalid demo access.");
        return starts.computeIfAbsent(requestId, key -> new Attempt("demo-" + key,
                "SEAL practice examination", Instant.now().plusSeconds(900), List.of(
                new Question("q1", QuestionType.MCQ, "Which component handles an HTTP request?", 1,
                        List.of(new Option("a", "REST controller"), new Option("b", "Database table"), new Option("c", "CSS stylesheet"))),
                new Question("q2", QuestionType.TEXT, "Explain the purpose of a client service boundary.", 5, List.of()),
                new Question("q3", QuestionType.MCQ, "Which format does the SEAL client use for API data?", 1,
                        List.of(new Option("a", "JSON"), new Option("b", "FXML"), new Option("c", "CSS"))))));
    }
    public Receipt submit(Submission submission, String token) {
        if (starts.values().stream().noneMatch(a -> a.id().equals(submission.attemptId())))
            throw new IllegalArgumentException("Unknown demo attempt.");
        return receipts.computeIfAbsent(submission.requestId(), key -> new Receipt("DEMO-" + key,
                "Demo submission complete. No answers were saved to the server and no grade was issued."));
    }
}
