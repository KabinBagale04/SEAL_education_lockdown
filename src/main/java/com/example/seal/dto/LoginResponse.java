package com.example.seal.dto;

public record LoginResponse(
        boolean success,
        Long userId,
        String fullName,
        String role,
        String token,
        String message
) {
}
