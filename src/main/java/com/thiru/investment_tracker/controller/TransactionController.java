package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.BulkGetRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/download")
    public ResponseEntity<InputStreamResource> downloadPortfolioStocks(@PathVariable String email) {

        Pair<InputStreamResource, String> resourcePair = transactionService.downloadAllTransactions(UserMail.from(email));

        String mediaType = "application/vnd.ms-excel";
        String headerValue = "attachment; filename=" + resourcePair.getSecond();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(MediaType.parseMediaType(mediaType)).body(resourcePair.getFirst());
    }
}
