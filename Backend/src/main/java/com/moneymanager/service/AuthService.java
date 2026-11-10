package com.moneymanager.service;

import com.moneymanager.dto.AuthDtos.*;
import com.moneymanager.entity.Category;
import com.moneymanager.entity.PasswordResetToken;
import com.moneymanager.entity.TransactionType;
import com.moneymanager.entity.User;
import com.moneymanager.repository.CategoryRepository;
import com.moneymanager.repository.PasswordResetTokenRepository;
import com.moneymanager.repository.UserRepository;
import com.moneymanager.security.JwtTokenProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JavaMailSender mailSender;

    public AuthService(UserRepository userRepository,
                       CategoryRepository categoryRepository,
                       PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.mailSender = mailSender;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .currency("USD ($)")
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Seed initial default categories for the new user
        seedDefaultCategories(user);

        String token = tokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, mapToUserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, mapToUserResponse(user));
    }

    public void sendOtp(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account with this email does not exist"));

        // Generate 6-digit OTP
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));

        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .otp(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(token);

        // Send Email (or log in dev)
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("MoneyManager - Password Reset OTP");
            message.setText("Your OTP verification code for password reset is: " + otp + "\nIt will expire in 15 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Note: MailSender failed, printing OTP to log: " + otp);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository
                .findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No active OTP request found for this email"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!resetToken.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP code!");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(String email, UserResponse updateDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateDto.getFullName() != null) user.setFullName(updateDto.getFullName());
        if (updateDto.getCurrency() != null) user.setCurrency(updateDto.getCurrency());
        if (updateDto.getProfileImageUrl() != null) user.setProfileImageUrl(updateDto.getProfileImageUrl());

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setCurrency(user.getCurrency());
        res.setProfileImageUrl(user.getProfileImageUrl());
        return res;
    }

    private void seedDefaultCategories(User user) {
        List<Category> defaults = List.of(
                Category.builder().user(user).name("Salary").type(TransactionType.INCOME).icon("💰").colorHex("#10b981").build(),
                Category.builder().user(user).name("Freelance").type(TransactionType.INCOME).icon("💻").colorHex("#3b82f6").build(),
                Category.builder().user(user).name("Food & Dining").type(TransactionType.EXPENSE).icon("🍔").colorHex("#ef4444").build(),
                Category.builder().user(user).name("Housing & Rent").type(TransactionType.EXPENSE).icon("🏠").colorHex("#f59e0b").build(),
                Category.builder().user(user).name("Transportation").type(TransactionType.EXPENSE).icon("🚗").colorHex("#8b5cf6").build(),
                Category.builder().user(user).name("Entertainment").type(TransactionType.EXPENSE).icon("🎬").colorHex("#ec4899").build()
        );
        categoryRepository.saveAll(defaults);
    }
}

