package com.javaproject.authorization_decisioning.dto;

public record LoginRequest(
        String username,
        String password
) {
}