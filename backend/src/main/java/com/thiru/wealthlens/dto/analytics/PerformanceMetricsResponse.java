package com.thiru.wealthlens.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceMetricsResponse {
    private Long totalTrades;
    private Long winCount;
    private Long lossCount;
    private Long breakevenCount;
    private Double averageProfitPerWin;
    private Double averageLossPerLoss;
    private Double winLossRatio;
    private Double averageHoldingPeriodDays;
    private Double portfolioTurnover;
    private StockPerformance bestPerformingStock;
    private StockPerformance worstPerformingStock;
}
