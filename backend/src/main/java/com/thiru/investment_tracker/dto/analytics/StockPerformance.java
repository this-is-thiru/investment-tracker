package com.thiru.investment_tracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPerformance {
    private String stockCode;
    private String stockName;
    private Double returnPercentage;
    private Double absoluteReturn;
}
