package com.example.seal.dto;

public record CreateUserRequest(
        String fullName,
        String username,
        String password,
        String role
) {
}