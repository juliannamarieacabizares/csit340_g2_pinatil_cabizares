package com.cituccs.sims.web;

public final class AuthModels {
    private AuthModels() {
    }

    public record RegisterRequest(String name, String email, String password) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record PasswordResetRequest(String email) {
    }

    public record PasswordUpdateRequest(String token, String password) {
    }

    public record UserResponse(Long id, String name, String email) {
    }
}