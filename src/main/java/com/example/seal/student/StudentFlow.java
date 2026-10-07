package com.example.seal.student;
import com.example.seal.Navigator;
import com.example.seal.session.UserSession;
import com.example.seal.student.model.ExamDtos.*;
import com.example.seal.student.service.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** One in-memory attempt. Authentication remains owned by UserSession. */
public final class StudentFlow {
    private static final StudentFlow INSTANCE = new StudentFlow();
    public final ExamService service = Boolean.getBoolean("seal.student.demo") ? new DemoExamService() : new PendingExamService();
    public ExamAccess access;
    public Attempt attempt;
    public Receipt receipt;
    public final Map<String, Answer> answers = new LinkedHashMap<>();
    public String startRequestId;
    public String submitRequestId;
    private Long owner;
    private String sessionToken;
    private StudentFlow() { }
    public static StudentFlow current() {
        if (!"STUDENT".equals(UserSession.getRole()) || UserSession.getToken() == null || UserSession.getToken().isBlank()) {
            INSTANCE.reset();
            throw new IllegalStateException("Please sign in with a student account.");
        }
        if (!java.util.Objects.equals(INSTANCE.owner, UserSession.getUserId())
                || !java.util.Objects.equals(INSTANCE.sessionToken, UserSession.getToken())) {
            INSTANCE.reset();
            INSTANCE.owner = UserSession.getUserId();
            INSTANCE.sessionToken = UserSession.getToken();
        }
        return INSTANCE;
    }
    public void reset() {
        access = null; attempt = null; receipt = null; answers.clear();
        startRequestId = UUID.randomUUID().toString();
        submitRequestId = UUID.randomUUID().toString();
    }
    public void go(String page) { Navigator.goTo("student/" + page); }
    public void returnToAccess() { reset(); Navigator.goTo("student-home"); }
    public Submission submission() {
        return new Submission(attempt.id(), submitRequestId, attempt.questions().stream()
                .map(q -> answers.getOrDefault(q.id(), new Answer(q.id(), null, ""))).toList());
    }
    public long answeredCount() {
        return answers.values().stream().filter(a -> a.selectedOptionId() != null
                || (a.textAnswer() != null && !a.textAnswer().isBlank())).count();
    }
}
