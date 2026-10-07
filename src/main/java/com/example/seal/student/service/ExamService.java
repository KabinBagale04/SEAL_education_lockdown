package com.example.seal.student.service;
import com.example.seal.student.model.ExamDtos.*;

/** Blocking boundary; invoke off the JavaFX application thread. */
public interface ExamService {
    ExamAccess validateAccess(AccessRequest request, String bearerToken) throws Exception;
    Attempt startExam(String accessId, String requestId, String bearerToken) throws Exception;
    Receipt submit(Submission submission, String bearerToken) throws Exception;
    default boolean isDemo() { return false; }
}
