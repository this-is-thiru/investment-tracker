package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.service.PortfolioService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RequestMapping("/portfolio/user/{email}")
@RestController
public class SpecialController {
    private final PortfolioService portfolioService;

    @PostMapping("/update")
    public ResponseEntity<String> testRequest(@PathVariable String email) {

        portfolioService.updateTransactions();
        return ResponseEntity.ok("Hey! Message seeded for process");
    }
}
