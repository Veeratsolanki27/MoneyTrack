package com.moneymanager.service;

import com.moneymanager.dto.TransactionDtos.*;
import com.moneymanager.entity.Category;
import com.moneymanager.entity.Transaction;
import com.moneymanager.entity.TransactionType;
import com.moneymanager.entity.User;
import com.moneymanager.repository.CategoryRepository;
import com.moneymanager.repository.TransactionRepository;
import com.moneymanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<TransactionResponse> getFiltered(String email, TransactionType type, Long categoryId,
                                                LocalDate startDate, LocalDate endDate, String search) {
        User user = getUser(email);
        return transactionRepository.findFiltered(user.getId(), type, categoryId, startDate, endDate, search)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse create(String email, TransactionRequest request) {
        User user = getUser(email);
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .type(request.getType())
                .amount(request.getAmount())
                .title(request.getTitle())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH")
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long id, String email, TransactionRequest request) {
        User user = getUser(email);
        Transaction tx = transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));

        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findById(request.getCategoryId()).orElse(null);
            tx.setCategory(cat);
        }
        if (request.getTitle() != null) tx.setTitle(request.getTitle());
        if (request.getAmount() != null) tx.setAmount(request.getAmount());
        if (request.getType() != null) tx.setType(request.getType());
        if (request.getTransactionDate() != null) tx.setTransactionDate(request.getTransactionDate());
        if (request.getDescription() != null) tx.setDescription(request.getDescription());
        if (request.getPaymentMethod() != null) tx.setPaymentMethod(request.getPaymentMethod());

        return mapToResponse(transactionRepository.save(tx));
    }

    @Transactional
    public void delete(Long id, String email) {
        User user = getUser(email);
        Transaction tx = transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));
        transactionRepository.delete(tx);
    }

    public DashboardSummaryResponse getDashboardSummary(String email) {
        User user = getUser(email);
        List<Transaction> all = transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId());

        BigDecimal totalIncome = all.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBalance = totalIncome.subtract(totalExpenses);
        BigDecimal monthlySavings = totalBalance.compareTo(BigDecimal.ZERO) > 0 ? totalBalance : BigDecimal.ZERO;

        // Calculate 12-month data
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<MonthlyStat> monthlyStats = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        for (int m = 1; m <= 12; m++) {
            final int monthVal = m;
            BigDecimal inc = all.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME
                            && t.getTransactionDate().getYear() == currentYear
                            && t.getTransactionDate().getMonthValue() == monthVal)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal exp = all.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE
                            && t.getTransactionDate().getYear() == currentYear
                            && t.getTransactionDate().getMonthValue() == monthVal)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyStats.add(new MonthlyStat(months[m - 1], inc, exp));
        }

        // Category breakdown
        Map<String, BigDecimal> catTotals = new HashMap<>();
        Map<String, String> catIcons = new HashMap<>();
        Map<String, String> catColors = new HashMap<>();

        all.stream().filter(t -> t.getType() == TransactionType.EXPENSE).forEach(t -> {
            String name = t.getCategory() != null ? t.getCategory().getName() : "General";
            String icon = t.getCategory() != null ? t.getCategory().getIcon() : "🏷️";
            String color = t.getCategory() != null ? t.getCategory().getColorHex() : "#64748b";

            catTotals.merge(name, t.getAmount(), BigDecimal::add);
            catIcons.putIfAbsent(name, icon);
            catColors.putIfAbsent(name, color);
        });

        List<CategoryStat> categoryBreakdown = new ArrayList<>();
        catTotals.forEach((name, amount) -> {
            int pct = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(totalExpenses, 0, RoundingMode.HALF_UP).intValue()
                    : 0;
            categoryBreakdown.add(new CategoryStat(name, catIcons.get(name), catColors.get(name), amount, pct));
        });

        List<TransactionResponse> recent = all.stream().limit(5).map(this::mapToResponse).collect(Collectors.toList());

        DashboardSummaryResponse res = new DashboardSummaryResponse();
        res.setTotalBalance(totalBalance);
        res.setTotalIncome(totalIncome);
        res.setTotalExpenses(totalExpenses);
        res.setMonthlySavings(monthlySavings);
        res.setMonthlyStats(monthlyStats);
        res.setCategoryBreakdown(categoryBreakdown);
        res.setRecentTransactions(recent);
        return res;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TransactionResponse mapToResponse(Transaction t) {
        TransactionResponse res = new TransactionResponse();
        res.setId(t.getId());
        res.setTitle(t.getTitle());
        res.setAmount(t.getAmount());
        res.setType(t.getType());
        res.setTransactionDate(t.getTransactionDate());
        res.setDescription(t.getDescription());
        res.setPaymentMethod(t.getPaymentMethod());
        if (t.getCategory() != null) {
            CategoryResponse c = new CategoryResponse();
            c.setId(t.getCategory().getId());
            c.setName(t.getCategory().getName());
            c.setType(t.getCategory().getType());
            c.setIcon(t.getCategory().getIcon());
            c.setColorHex(t.getCategory().getColorHex());
            res.setCategory(c);
        }
        return res;
    }
}

