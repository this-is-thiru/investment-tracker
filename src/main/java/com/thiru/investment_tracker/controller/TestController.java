package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.BulkGetRequest;
import com.thiru.investment_tracker.dto.ProfitLossDto;
import com.thiru.investment_tracker.dto.TransactionResponse;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.service.PortfolioService;
import com.thiru.investment_tracker.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/test/user/{email}")
@RestController
public class TestController {
    private final PortfolioService portfolioService;

    @PostMapping("/transact/sell")
    public void getUserTransaction(@PathVariable String email, @RequestBody ProfitLossDto dto) {

        String transactionId = dto.transactionId();
        AssetRequest assetRequest = dto.assetRequest();
        List<AssetEntity> stockEntities = dto.stockEntities();

        portfolioService.updateQuantityBySavingReportAndProfitAndLoss1(UserMail.from(email), transactionId, stockEntities, assetRequest);
    }
}
