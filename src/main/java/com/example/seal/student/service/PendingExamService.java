package com.example.seal.student.service;
import com.example.seal.student.model.ExamDtos.*;

/** Replace with a REST adapter after student endpoints and DTOs are agreed. */
public final class PendingExamService implements ExamService {
    private IllegalStateException unavailable() {
        return new IllegalStateException("Student exam access is not available yet. Please contact your teacher.");
    }
    public ExamAccess validateAccess(AccessRequest request, String token) { throw unavailable(); }
    public Attempt startExam(String accessId, String requestId, String token) { throw unavailable(); }
    public Receipt submit(Submission submission, String token) { throw unavailable(); }
}
