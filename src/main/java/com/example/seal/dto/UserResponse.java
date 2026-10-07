package com.example.seal.dto;

public record UserResponse(
        Long id,
        String fullName,
        String username,
        String role,
        boolean active
) {
}