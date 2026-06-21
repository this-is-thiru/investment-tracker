package com.thiru.wealthlens.controller;

import com.thiru.wealthlens.shared.dto.BulkGetRequest;
import com.thiru.wealthlens.dto.TransactionResponse;
import com.thiru.wealthlens.shared.dto.user.UserMail;
import com.thiru.wealthlens.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
