package com.thiru.investment_tracker.dto.analytics;

import com.thiru.investment_tracker.dto.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetAllocationResponse {
    private AssetType assetType;
    private Double investedValue;
    private Double currentValue;
    private Double percentage;
}