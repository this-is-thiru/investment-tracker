package com.thiru.wealthlens.controller;

import com.thiru.wealthlens.dto.AssetRequest;
import com.thiru.wealthlens.dto.helper.ProfitLossDto;
import com.thiru.wealthlens.dto.user.UserMail;
import com.thiru.wealthlens.entity.AssetEntity;
import com.thiru.wealthlens.service.PortfolioService;
import lombok.AllArgsConstructor;
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
