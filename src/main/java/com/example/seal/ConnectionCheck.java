package com.example.seal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Security;
import java.time.Duration;
import java.time.Instant;

/** Credential-free HTTPS probe, also run against the packaged runtime in CI. */
public final class ConnectionCheck {
    public static void main(String[] args) throws Exception {
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("UTC time: " + Instant.now());
        System.out.println("SunEC available: " + (Security.getProvider("SunEC") != null));
        if (Security.getProvider("SunEC") == null) {
            throw new IllegalStateException("Missing SunEC TLS provider in the packaged runtime.");
        }
        String base = System.getProperty("seal.api.baseUrl", "https://seal-server.onrender.com").replaceAll("/+$", "");
        URI uri = URI.create(base + "/api/health");
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Connection check requires an HTTPS URL without credentials.");
        }
        System.out.println("Server host: " + uri.getHost());
        try (HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20)).build()) {
            var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(90)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());
            System.out.println("Health HTTP status: " + response.statusCode());
            var session = response.sslSession().orElseThrow();
            System.out.println("TLS: " + session.getProtocol() + "; cipher: " + session.getCipherSuite());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("HTTPS succeeded, but server health returned " + response.statusCode());
            }
            System.out.println("HTTPS connection and certificate verification succeeded.");
        }
    }
}
