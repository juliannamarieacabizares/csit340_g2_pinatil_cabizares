package com.cituccs.sims.service;

import com.cituccs.sims.entity.PasswordResetToken;
import com.cituccs.sims.entity.User;
import com.cituccs.sims.repository.PasswordResetTokenRepository;
import com.cituccs.sims.repository.UserRepository;
import com.cituccs.sims.web.AuthModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String INVALID_CREDENTIALS = "Invalid email or password.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String frontendUrl;
    private final String mailFrom;

    public AuthService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            ObjectProvider<JavaMailSender> mailSenders,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.mail.from}") String mailFrom) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenders = mailSenders;
        this.frontendUrl = frontendUrl;
        this.mailFrom = mailFrom;
    }

    public AuthModels.UserResponse authenticate(String email, String password) {
        if (email == null || password == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS));
        if (!verifyAndUpgradePassword(user, password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }
        return UserService.toResponse(user);
    }

    public AuthModels.UserResponse getUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserService::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
    }

    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Instant now = Instant.now();
        tokenRepository.deleteAllByExpiresAtBefore(now);
        userRepository.findByEmailIgnoreCase(normalizeEmail(email)).ifPresent(user -> sendResetEmail(user, now));
    }

    @Transactional
    public void completePasswordReset(String rawToken, String newPassword) {
        if (newPassword == null || newPassword.length() < 8
                || newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be between 8 and 72 UTF-8 bytes.");
        }
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reset link is invalid or expired.");
        }

        PasswordResetToken token = tokenRepository
                .findByTokenHashAndExpiresAtAfter(hashToken(rawToken), Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reset link is invalid or expired."));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteAllByUser_Id(user.getId());
    }

    private void sendResetEmail(User user, Instant now) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        tokenRepository.deleteAllByUser_Id(user.getId());
        tokenRepository.save(new PasswordResetToken(
                user,
                hashToken(rawToken),
                now.plus(30, ChronoUnit.MINUTES)));

        String separator = frontendUrl.contains("?") ? "&" : "?";
        String resetUrl = frontendUrl + separator + "resetToken="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Reset your Test Account password");
        message.setText("Use this link within 30 minutes to set a new password:\n\n" + resetUrl
                + "\n\nIf you did not request this, you can ignore this email.");

        JavaMailSender mailSender = mailSenders.getIfAvailable();
        if (mailSender == null) {
            tokenRepository.deleteAllByUser_Id(user.getId());
            logger.warn("Unable to send a password reset email. Check the SMTP configuration.");
            return;
        }

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            tokenRepository.deleteAllByUser_Id(user.getId());
            logger.warn("Unable to send a password reset email. Check the SMTP configuration.");
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean verifyAndUpgradePassword(User user, String submittedPassword) {
        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            return false;
        }
        if (storedPassword.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$")) {
            if (!passwordEncoder.matches(submittedPassword, storedPassword)) {
                return false;
            }
            if (passwordEncoder.upgradeEncoding(storedPassword)) {
                user.setPassword(passwordEncoder.encode(submittedPassword));
                userRepository.save(user);
            }
            return true;
        }

        boolean matchesLegacyPassword = MessageDigest.isEqual(
                storedPassword.getBytes(StandardCharsets.UTF_8),
                submittedPassword.getBytes(StandardCharsets.UTF_8));
        if (matchesLegacyPassword) {
            user.setPassword(passwordEncoder.encode(submittedPassword));
            userRepository.save(user);
        }
        return matchesLegacyPassword;
    }

    private static String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}