package com.thiru.wealthlens.corporate.dto.context;

public record DemergedStockContext(
        String stockCode,
        String stockName,
        double pricePercentage,
        double quantityRatio
) {
}
