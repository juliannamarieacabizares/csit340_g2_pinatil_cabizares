package com.cituccs.sims.service;

import com.cituccs.sims.entity.User;
import com.cituccs.sims.repository.UserRepository;
import com.cituccs.sims.web.AuthModels;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthModels.UserResponse registerUser(AuthModels.RegisterRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()
                || request.email() == null || request.email().isBlank()
                || !request.email().contains("@")
                || request.password() == null || request.password().length() < 8
                || request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a name, valid email, and password of 8 to 72 characters.");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }

        User user = new User(request.name().trim(), email, passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    public static AuthModels.UserResponse toResponse(User user) {
        return new AuthModels.UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}