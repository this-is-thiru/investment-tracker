package com.thiru.investment_tracker.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "from")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(staticName = "empty")
public class ReportContext extends AssetContext {
    private String stockCode;
    private String stockName;
    private String exchangeName;
    private String brokerName;
    private Long quantity;
    private double totalValue;
    private Enums.AssetType assetType;
    private String actorName;
}
