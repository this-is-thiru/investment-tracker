package com.thiru.investment_tracker.dto.context;

import com.thiru.investment_tracker.entity.AssetEntity;

import java.util.List;

public record DemergerContext(
        String stockCode,
        String stockName,
        double pricePercentage,
        double quantityRatio,
        AssetEntity entity,
        List<DemergedStockContext> demergedStocks
) {
}
