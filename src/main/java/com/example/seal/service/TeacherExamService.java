package com.example.seal.service;
import com.example.seal.model.Exam;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

public final class TeacherExamService {
    private final ApiClient api = new ApiClient();
    public List<Exam> list(String token) throws Exception {
        return Arrays.asList(ApiClient.JSON.treeToValue(api.request("GET", "/api/exams", null, token), Exam[].class));
    }
    public Exam save(Exam exam, String token) throws Exception {
        String path = "/api/exams" + (exam.getId() == null ? "" : "/" + exam.getId());
        return ApiClient.JSON.treeToValue(api.request(exam.getId() == null ? "POST" : "PUT", path, exam, token), Exam.class);
    }
    public Exam change(Long id, String action, String token) throws Exception {
        return ApiClient.JSON.treeToValue(api.request("PATCH", "/api/exams/" + id + "/" + action, null, token), Exam.class);
    }
    public JsonNode submissions(Long id, String token) throws Exception { return api.request("GET", "/api/exams/" + id + "/submissions", null, token); }
    public JsonNode detail(Long exam, long id, String token) throws Exception { return api.request("GET", "/api/exams/" + exam + "/submissions/" + id, null, token); }
    public JsonNode grade(Long exam, long id, List<?> grades, String token) throws Exception {
        return api.request("PATCH", "/api/exams/" + exam + "/submissions/" + id + "/grade", grades, token);
    }
}
