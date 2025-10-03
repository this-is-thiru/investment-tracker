package com.thiru.investment_tracker.dto;

import com.thiru.investment_tracker.entity.AssetEntity;

import java.util.List;

public record ProfitLossDto(
        String transactionId,
        AssetRequest assetRequest,
        List<AssetEntity> stockEntities
) {
}
