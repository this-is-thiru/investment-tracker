package com.thiru.investment_tracker.core.controller;

import com.thiru.investment_tracker.core.dto.BulkGetRequest;
import com.thiru.investment_tracker.core.dto.TransactionResponse;
import com.thiru.investment_tracker.core.dto.user.UserMail;
import com.thiru.investment_tracker.core.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/transactions/user/{email}")
@RestController
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public List<TransactionResponse> getUserTransaction(@PathVariable String email, @RequestBody BulkGetRequest bulkGetRequest) {
        return transactionService.userTransactions(UserMail.from(email), bulkGetRequest.getQueryFilters());
    }

    @GetMapping
    public List<TransactionResponse> getUserTransaction(@PathVariable String email) {
        return transactionService.getAllUserTransactions(UserMail.from(email));
    }
}
