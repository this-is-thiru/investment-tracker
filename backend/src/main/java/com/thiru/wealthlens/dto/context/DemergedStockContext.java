package com.thiru.wealthlens.dto.context;

public record DemergedStockContext(
        String stockCode,
        String stockName,
        double pricePercentage,
        double quantityRatio
) {
}
