package com.thiru.investment_tracker.dto.helper;

import com.thiru.investment_tracker.dto.request.AssetRequest;
import com.thiru.investment_tracker.entity.AssetEntity;

import java.util.List;

public record ProfitLossDto(
        String transactionId,
        AssetRequest assetRequest,
        List<AssetEntity> stockEntities
) {
}
