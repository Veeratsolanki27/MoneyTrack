package com.moneymanager.dto;

import com.moneymanager.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionDtos {

    public static class TransactionRequest {
        private String title;
        private BigDecimal amount;
        private TransactionType type;
        private Long categoryId;
        private LocalDate transactionDate;
        private String description;
        private String paymentMethod;

        public TransactionRequest() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class TransactionResponse {
        private Long id;
        private String title;
        private BigDecimal amount;
        private TransactionType type;
        private CategoryResponse category;
        private LocalDate transactionDate;
        private String description;
        private String paymentMethod;

        public TransactionResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }

        public CategoryResponse getCategory() { return category; }
        public void setCategory(CategoryResponse category) { this.category = category; }

        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class CategoryRequest {
        private String name;
        private TransactionType type;
        private String icon;
        private String colorHex;

        public CategoryRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public String getColorHex() { return colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    }

    public static class CategoryResponse {
        private Long id;
        private String name;
        private TransactionType type;
        private String icon;
        private String colorHex;

        public CategoryResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public String getColorHex() { return colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    }

    public static class DashboardSummaryResponse {
        private BigDecimal totalBalance;
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal monthlySavings;
        private List<MonthlyStat> monthlyStats;
        private List<CategoryStat> categoryBreakdown;
        private List<TransactionResponse> recentTransactions;

        public DashboardSummaryResponse() {}

        public BigDecimal getTotalBalance() { return totalBalance; }
        public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

        public BigDecimal getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

        public BigDecimal monthlySavings() { return monthlySavings; }
        public BigDecimal getMonthlySavings() { return monthlySavings; }
        public void setMonthlySavings(BigDecimal monthlySavings) { this.monthlySavings = monthlySavings; }

        public List<MonthlyStat> getMonthlyStats() { return monthlyStats; }
        public void setMonthlyStats(List<MonthlyStat> monthlyStats) { this.monthlyStats = monthlyStats; }

        public List<CategoryStat> getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(List<CategoryStat> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

        public List<TransactionResponse> getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(List<TransactionResponse> recentTransactions) { this.recentTransactions = recentTransactions; }
    }

    public static class MonthlyStat {
        private String month;
        private BigDecimal income;
        private BigDecimal expense;

        public MonthlyStat() {}

        public MonthlyStat(String month, BigDecimal income, BigDecimal expense) {
            this.month = month;
            this.income = income;
            this.expense = expense;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public BigDecimal getIncome() { return income; }
        public void setIncome(BigDecimal income) { this.income = income; }

        public BigDecimal getExpense() { return expense; }
        public void setExpense(BigDecimal expense) { this.expense = expense; }
    }

    public static class CategoryStat {
        private String name;
        private String icon;
        private String color;
        private BigDecimal amount;
        private Integer percentage;

        public CategoryStat() {}

        public CategoryStat(String name, String icon, String color, BigDecimal amount, Integer percentage) {
            this.name = name;
            this.icon = icon;
            this.color = color;
            this.amount = amount;
            this.percentage = percentage;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public Integer getPercentage() { return percentage; }
        public void setPercentage(Integer percentage) { this.percentage = percentage; }
    }
}
