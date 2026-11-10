package com.moneymanager.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDate transactionDate;

    private String paymentMethod = "CASH";

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Transaction() {}

    public Transaction(Long id, User user, Category category, TransactionType type, BigDecimal amount,
                       String title, String description, LocalDate transactionDate, String paymentMethod) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.title = title;
        this.description = description;
        this.transactionDate = transactionDate;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    public static class TransactionBuilder {
        private Long id;
        private User user;
        private Category category;
        private TransactionType type;
        private BigDecimal amount;
        private String title;
        private String description;
        private LocalDate transactionDate;
        private String paymentMethod = "CASH";

        public TransactionBuilder id(Long id) { this.id = id; return this; }
        public TransactionBuilder user(User user) { this.user = user; return this; }
        public TransactionBuilder category(Category category) { this.category = category; return this; }
        public TransactionBuilder type(TransactionType type) { this.type = type; return this; }
        public TransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionBuilder title(String title) { this.title = title; return this; }
        public TransactionBuilder description(String description) { this.description = description; return this; }
        public TransactionBuilder transactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; return this; }
        public TransactionBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }

        public Transaction build() {
            return new Transaction(id, user, category, type, amount, title, description, transactionDate, paymentMethod);
        }
    }
}
