package com.moneymanager.service;

import com.moneymanager.entity.Transaction;
import com.moneymanager.entity.User;
import com.moneymanager.repository.TransactionRepository;
import com.moneymanager.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Service
public class ReportsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ReportsService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public void writeCsv(String userEmail, Writer writer) throws IOException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId());

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Title", "Amount", "Type", "Category", "Date", "PaymentMethod", "Description")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Transaction t : transactions) {
                printer.printRecord(
                        t.getId(),
                        t.getTitle(),
                        t.getAmount(),
                        t.getType(),
                        t.getCategory() != null ? t.getCategory().getName() : "General",
                        t.getTransactionDate(),
                        t.getPaymentMethod(),
                        t.getDescription() != null ? t.getDescription() : ""
                );
            }
        }
    }
}

