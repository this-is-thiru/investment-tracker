package com.thiru.investment_tracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketPriceEntry {
    private String stockCode;
    private Double price;
}
