package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.BulkGetRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/transactions/user/{email}")
@RestController
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public List<Transaction> getUserTransaction(@PathVariable String email, @RequestBody BulkGetRequest bulkGetRequest) {
        return transactionService.getUserTransactions(UserMail.from(email), bulkGetRequest.getQueryFilters());
    }
}
