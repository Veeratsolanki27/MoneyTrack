package com.moneymanager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private Boolean used = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PasswordResetToken() {}

    public PasswordResetToken(Long id, String email, String otp, LocalDateTime expiryDate, Boolean used) {
        this.id = id;
        this.email = email;
        this.otp = otp;
        this.expiryDate = expiryDate;
        this.used = used != null ? used : false;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public static PasswordResetTokenBuilder builder() {
        return new PasswordResetTokenBuilder();
    }

    public static class PasswordResetTokenBuilder {
        private Long id;
        private String email;
        private String otp;
        private LocalDateTime expiryDate;
        private Boolean used = false;

        public PasswordResetTokenBuilder id(Long id) { this.id = id; return this; }
        public PasswordResetTokenBuilder email(String email) { this.email = email; return this; }
        public PasswordResetTokenBuilder otp(String otp) { this.otp = otp; return this; }
        public PasswordResetTokenBuilder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public PasswordResetTokenBuilder used(Boolean used) { this.used = used; return this; }

        public PasswordResetToken build() {
            return new PasswordResetToken(id, email, otp, expiryDate, used);
        }
    }
}
