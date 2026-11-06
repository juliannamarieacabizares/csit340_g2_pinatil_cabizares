package com.cituccs.sims.controller;

import com.cituccs.sims.service.AuthService;
import com.cituccs.sims.web.AuthModels;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String USER_ID_ATTRIBUTE = "authenticatedUserId";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthModels.UserResponse login(
            @RequestBody AuthModels.LoginRequest credentials,
            HttpServletRequest request) {
        AuthModels.UserResponse user = authService.authenticate(credentials.email(), credentials.password());
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(USER_ID_ATTRIBUTE, user.id());
        return user;
    }

    @GetMapping("/me")
    public AuthModels.UserResponse currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(USER_ID_ATTRIBUTE) instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in.");
        }
        return authService.getUser(userId);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/request")
    public Map<String, String> requestPasswordReset(@RequestBody AuthModels.PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return Map.of("message", "If an account exists for that email, reset instructions will be sent.");
    }

    @PostMapping("/password-reset/complete")
    public Map<String, String> completePasswordReset(@RequestBody AuthModels.PasswordUpdateRequest request) {
        authService.completePasswordReset(request.token(), request.password());
        return Map.of("message", "Your password has been updated. You can now sign in.");
    }
}