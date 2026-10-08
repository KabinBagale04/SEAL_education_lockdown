package com.example.seal.service;

import com.fasterxml.jackson.databind.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.io.IOException;
import javax.net.ssl.SSLException;

/** Shared transport only; authentication still uses the existing opaque session token. */
public final class ApiClient {
    public static final ObjectMapper JSON = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private static final String BASE = System.getProperty("seal.api.baseUrl", "https://seal-server.onrender.com").replaceAll("/+$", "");
    public JsonNode request(String method, String path, Object body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(90))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
        HttpResponse<String> response;
        try {
            response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (SSLException error) {
            throw new IllegalStateException(ConnectionErrors.message(error), error);
        } catch (HttpTimeoutException error) {
            throw new IllegalStateException("Server request timed out. The server may be starting. Check the latest saved state before retrying a save or submission.", error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Server request was interrupted. Check the latest saved state before retrying.", error);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot reach the SEAL server. Check your connection. For a save or submission, check its status before retrying.", error);
        }
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
