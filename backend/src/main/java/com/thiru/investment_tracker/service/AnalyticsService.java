package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.analytics.AssetAllocationResponse;
import com.thiru.investment_tracker.dto.analytics.PerformanceMetricsResponse;
import com.thiru.investment_tracker.dto.analytics.PortfolioSummaryResponse;
import com.thiru.investment_tracker.dto.analytics.StockPerformance;
import com.thiru.investment_tracker.dto.analytics.XirrRequest;
import com.thiru.investment_tracker.dto.analytics.XirrResponse;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.entity.ProfitAndLossEntity;
import com.thiru.investment_tracker.entity.TradeOutcomeEntity;
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
import java.util.Collections;
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
        List<TradeOutcomeEntity> outcomes = tradeOutcomeRepository.findByEmail(userMail.getEmail());

        if (outcomes.isEmpty()) {
            return PerformanceMetricsResponse.builder()
                    .totalTrades(0L)
                    .winCount(0L)
                    .lossCount(0L)
                    .breakevenCount(0L)
                    .averageProfitPerWin(0.0)
                    .averageLossPerLoss(0.0)
                    .winLossRatio(0.0)
                    .averageHoldingPeriodDays(0.0)
                    .portfolioTurnover(0.0)
                    .build();
        }

        // Separate outcomes by profit category (treat null netProfit as 0)
        List<TradeOutcomeEntity> wins = outcomes.stream()
                .filter(o -> o.getNetProfit() > 0)
                .collect(Collectors.toList());
        List<TradeOutcomeEntity> losses = outcomes.stream()
                .filter(o -> o.getNetProfit() < 0)
                .collect(Collectors.toList());
        List<TradeOutcomeEntity> breakevens = outcomes.stream()
                .filter(o -> o.getNetProfit() == 0)
                .collect(Collectors.toList());

        long totalTrades = outcomes.size();
        long winCount = wins.size();
        long lossCount = losses.size();
        long breakevenCount = breakevens.size();

        // Averages
        double avgProfit = wins.stream().mapToDouble(TradeOutcomeEntity::getNetProfit).average().orElse(0.0);
        double avgLoss = losses.stream().mapToDouble(o -> Math.abs(o.getNetProfit())).average().orElse(0.0);

        double winLossRatio = lossCount > 0 ? (double) winCount / lossCount : 0.0;
        double avgHolding = outcomes.stream().mapToDouble(TradeOutcomeEntity::getHoldingPeriodDays).average().orElse(0.0);

        // Portfolio turnover = total sell value / total buy value
        double totalBuyValue = outcomes.stream().mapToDouble(TradeOutcomeEntity::getTotalBuyValue).sum();
        double totalSellValue = outcomes.stream().mapToDouble(TradeOutcomeEntity::getTotalSellValue).sum();
        double turnover = totalBuyValue > 0 ? totalSellValue / totalBuyValue : 0.0;

        // Best / worst stock by aggregated net profit
        Map<String, Double> profitByStock = outcomes.stream()
                .collect(Collectors.groupingBy(
                        TradeOutcomeEntity::getStockCode,
                        Collectors.summingDouble(TradeOutcomeEntity::getNetProfit)
                ));

        Map<String, Double> investedByStock = outcomes.stream()
                .collect(Collectors.groupingBy(
                        TradeOutcomeEntity::getStockCode,
                        Collectors.summingDouble(TradeOutcomeEntity::getTotalBuyValue)
                ));

        StockPerformance best = profitByStock.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> {
                    double invested = investedByStock.getOrDefault(e.getKey(), 1.0);
                    double pct = invested > 0 ? (e.getValue() / invested) * 100 : 0.0;
                    return new StockPerformance(e.getKey(), null, pct, e.getValue());
                })
                .orElse(null);

        StockPerformance worst = profitByStock.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(e -> {
                    double invested = investedByStock.getOrDefault(e.getKey(), 1.0);
                    double pct = invested > 0 ? (e.getValue() / invested) * 100 : 0.0;
                    return new StockPerformance(e.getKey(), null, pct, e.getValue());
                })
                .orElse(null);

        return PerformanceMetricsResponse.builder()
                .totalTrades(totalTrades)
                .winCount(winCount)
                .lossCount(lossCount)
                .breakevenCount(breakevenCount)
                .averageProfitPerWin(avgProfit)
                .averageLossPerLoss(avgLoss)
                .winLossRatio(winLossRatio)
                .averageHoldingPeriodDays(avgHolding)
                .portfolioTurnover(turnover)
                .bestPerformingStock(best)
                .worstPerformingStock(worst)
                .build();
    }

    public XirrResponse calculateXirr(UserMail userMail, XirrRequest request) {
        // Stub
        return XirrResponse.builder().build();
    }
}