package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.analytics.AssetAllocationResponse;
import com.thiru.investment_tracker.dto.analytics.PerformanceMetricsResponse;
import com.thiru.investment_tracker.dto.analytics.PortfolioSummaryResponse;
import com.thiru.investment_tracker.dto.analytics.XirrRequest;
import com.thiru.investment_tracker.dto.analytics.XirrResponse;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.entity.ProfitAndLossEntity;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.entity.model.RealisedProfits;
import com.thiru.investment_tracker.repository.PortfolioRepository;
import com.thiru.investment_tracker.repository.ProfitAndLossRepository;
import com.thiru.investment_tracker.repository.TradeOutcomeRepository;
import com.thiru.investment_tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final ProfitAndLossRepository profitAndLossRepository;
    private final TradeOutcomeRepository tradeOutcomeRepository;

    public PortfolioSummaryResponse getPortfolioSummary(UserMail userMail) {
        String email = userMail.getEmail();

        List<AssetEntity> assets = portfolioRepository.findByEmail(email);
        List<TransactionEntity> transactions = transactionRepository.findByEmail(email);
        List<ProfitAndLossEntity> pnlList = profitAndLossRepository.findAllByEmail(email);

        double totalInvested = assets.stream()
                .mapToDouble(a -> a.getPrice() * a.getQuantity())
                .sum();

        double totalRealizedProfit = pnlList.stream()
                .mapToDouble(pnl -> {
                    RealisedProfits realised = pnl.getRealisedProfits();
                    double st = (realised != null && realised.getShortTermCapitalGains() != null)
                            ? realised.getShortTermCapitalGains().getProfit() : 0.0;
                    double lt = (realised != null && realised.getLongTermCapitalGains() != null)
                            ? realised.getLongTermCapitalGains().getProfit() : 0.0;
                    return st + lt;
                })
                .sum();

        long totalTrades = transactions.size();
        long buyTrades = transactions.stream().filter(t -> t.getTransactionType() == TransactionType.BUY).count();
        long sellTrades = transactions.stream().filter(t -> t.getTransactionType() == TransactionType.SELL).count();
        long holdingCount = assets.stream().filter(a -> a.getQuantity() != null && a.getQuantity() > 0).count();

        return PortfolioSummaryResponse.builder()
                .totalInvested(totalInvested)
                .currentPortfolioValue(null)
                .totalRealizedProfit(totalRealizedProfit)
                .totalUnrealizedProfit(null)
                .overallReturnPercentage(null)
                .totalTrades(totalTrades)
                .buyTrades(buyTrades)
                .sellTrades(sellTrades)
                .holdingCount(holdingCount)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    public List<AssetAllocationResponse> getAssetAllocation(UserMail userMail) {
        String email = userMail.getEmail();
        List<AssetEntity> assets = portfolioRepository.findByEmail(email);

        Map<AssetType, Double> investedByType = assets.stream()
                .filter(a -> a.getAssetType() != null)
                .collect(Collectors.groupingBy(
                        AssetEntity::getAssetType,
                        Collectors.summingDouble(a -> a.getPrice() * a.getQuantity())
                ));

        double total = investedByType.values().stream().mapToDouble(Double::doubleValue).sum();

        return investedByType.entrySet().stream()
                .map(e -> new AssetAllocationResponse(
                        e.getKey(),
                        e.getValue(),
                        null,
                        total > 0 ? (e.getValue() / total) * 100 : 0.0
                ))
                .collect(Collectors.toList());
    }

    public PerformanceMetricsResponse getPerformanceMetrics(UserMail userMail) {
        // Stub
        return PerformanceMetricsResponse.builder().build();
    }

    public XirrResponse calculateXirr(UserMail userMail, XirrRequest request) {
        // Stub
        return XirrResponse.builder().build();
    }
}