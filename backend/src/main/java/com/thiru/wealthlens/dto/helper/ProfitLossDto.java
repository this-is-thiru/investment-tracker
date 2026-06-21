package com.thiru.wealthlens.dto.helper;

import com.thiru.wealthlens.dto.AssetRequest;
import com.thiru.wealthlens.entity.AssetEntity;

import java.util.List;

public record ProfitLossDto(
        String transactionId,
        AssetRequest assetRequest,
        List<AssetEntity> stockEntities
) {
}
