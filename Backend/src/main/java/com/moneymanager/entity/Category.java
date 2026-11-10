package com.moneymanager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String icon = "🏷️";
    private String colorHex = "#10b981";

    private LocalDateTime createdAt = LocalDateTime.now();

    public Category() {}

    public Category(Long id, User user, String name, TransactionType type, String icon, String colorHex) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.type = type;
        this.icon = icon != null ? icon : "🏷️";
        this.colorHex = colorHex != null ? colorHex : "#10b981";
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public static CategoryBuilder builder() {
        return new CategoryBuilder();
    }

    public static class CategoryBuilder {
        private Long id;
        private User user;
        private String name;
        private TransactionType type;
        private String icon = "🏷️";
        private String colorHex = "#10b981";

        public CategoryBuilder id(Long id) { this.id = id; return this; }
        public CategoryBuilder user(User user) { this.user = user; return this; }
        public CategoryBuilder name(String name) { this.name = name; return this; }
        public CategoryBuilder type(TransactionType type) { this.type = type; return this; }
        public CategoryBuilder icon(String icon) { this.icon = icon; return this; }
        public CategoryBuilder colorHex(String colorHex) { this.colorHex = colorHex; return this; }

        public Category build() {
            return new Category(id, user, name, type, icon, colorHex);
        }
    }
}
