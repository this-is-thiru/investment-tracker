package com.thiru.investment_tracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSummaryResponse {
    private Double totalInvested;
    private Double currentPortfolioValue;
    private Double totalRealizedProfit;
    private Double totalUnrealizedProfit;
    private Double overallReturnPercentage;
    private Long totalTrades;
    private Long buyTrades;
    private Long sellTrades;
    private Long holdingCount;
    private LocalDateTime lastUpdated;
}
