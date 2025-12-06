package com.thiru.investment_tracker.dto.context;

public record DemergedStockContext(String stockCode, String stockName, double pricePercentage, double quantityRatio) {
}