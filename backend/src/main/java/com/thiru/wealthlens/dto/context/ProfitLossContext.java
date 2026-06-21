package com.thiru.wealthlens.dto.context;

import com.thiru.wealthlens.dto.enums.AccountType;
import com.thiru.wealthlens.dto.enums.AssetType;
import com.thiru.wealthlens.dto.enums.BrokerName;
import com.thiru.wealthlens.dto.enums.CorporateActionType;
import com.thiru.wealthlens.dto.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public record ProfitLossContext(
        String transactionId,
        double quantity,
        LocalDate date,
        double price,
        String stockCode,
        BrokerName brokerName,
        String exchangeName,
        AssetType assetType,
        TransactionType transactionType,
        CorporateActionType actionType,
        AccountType accountType,
        String accountHolder,
        List<BuyContext> buyContexts
) {
}
