package com.moneymanager.controller;

import com.moneymanager.dto.TransactionDtos.TransactionRequest;
import com.moneymanager.dto.TransactionDtos.TransactionResponse;
import com.moneymanager.entity.TransactionType;
import com.moneymanager.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(transactionService.getFiltered(userEmail, type, categoryId, startDate, endDate, search));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestBody TransactionRequest request,
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(transactionService.create(userEmail, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @RequestBody TransactionRequest request,
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(transactionService.update(id, userEmail, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String userEmail) {
        transactionService.delete(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}

