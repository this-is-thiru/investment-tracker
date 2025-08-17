package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.TemporaryTransactionEntity;
import com.thiru.investment_tracker.service.PortfolioService;
import com.thiru.investment_tracker.service.TemporaryTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/temporary-transactions/user/{email}")
public class TemporaryTransactionsController {

    private final PortfolioService portfolioService;
    private final TemporaryTransactionService temporaryTransactionService;

    @GetMapping("/all")
    public List<TemporaryTransactionEntity> getAllTemporaryTransactions(@PathVariable String email) {
        return temporaryTransactionService.getAllTemporaryTransactions(UserMail.from(email));
    }

    @PostMapping("/redrive")
    public String redriveTemporaryTransactions(@PathVariable String email) {
        return portfolioService.redriveTemporaryTransactions(UserMail.from(email));
    }
}
