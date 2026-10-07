package com.example.seal.service;

import com.fasterxml.jackson.databind.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

/** Shared transport only; authentication still uses the existing opaque session token. */
public final class ApiClient {
    public static final ObjectMapper JSON = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final String BASE = System.getProperty("seal.api.baseUrl", "https://seal-server.onrender.com").replaceAll("/+$", "");
    public JsonNode request(String method, String path, Object body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE + path)).timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
        HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String message = "Request failed (HTTP " + response.statusCode() + ").";
            try {
                JsonNode error = JSON.readTree(response.body());
                if (error.hasNonNull("message")) message = error.get("message").asText();
                else if (error.isTextual()) message = error.asText();
            } catch (Exception ignored) {
                if (response.statusCode() == 400 && response.body().length() < 300) message = response.body();
            }
            if (response.statusCode() == 401) message = path.equals("/api/auth/login") ? "Incorrect credentials or inactive account." : "Session expired or account inactive. Please sign in again.";
            throw new IllegalStateException(message);
        }
        return response.body().isBlank() ? JSON.nullNode() : JSON.readTree(response.body());
    }
}
