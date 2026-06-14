package com.thiru.investment_tracker.dto.context;

import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.CapitalGainsType;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class TradeOutcomeContext {

    // Identity fields
    private String email;
    private String stockCode;
    private String stockName;
    private String exchangeName;
    private BrokerName brokerName;
    private AssetType assetType;
    private AccountType accountType;
    private String accountHolder;

    // Buy side
    private double originalBuyPrice;
    private double caAdjustedBuyPrice;
    private Double buyQuantity;
    private LocalDate buyDate;
    private double buyBrokerCharges;
    private double buyMiscCharges;

    // Sell side
    private double sellPrice;
    private Double sellQuantity;
    private LocalDate sellDate;
    private double sellBrokerCharges;
    private double sellMiscCharges;

    // Computed fields
    private double totalBuyValue;
    private double totalSellValue;
    private double netProfit;
    private double profitPercentage;
    private Long holdingPeriodDays;
    private CapitalGainsType capitalGainsType;
    private String financialYear;

    // Linkage fields
    private String sourceSellTransactionId;
    private String sourceBuyLotId;

    // CA tracking
    private Boolean isCaDerived;
    private List<CorporateActionEntity> appliedCorporateActions;
}