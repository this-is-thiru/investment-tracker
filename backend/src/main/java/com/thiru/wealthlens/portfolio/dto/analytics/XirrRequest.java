package com.thiru.wealthlens.portfolio.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XirrRequest {
    private List<MarketPriceEntry> currentPrices;
}
