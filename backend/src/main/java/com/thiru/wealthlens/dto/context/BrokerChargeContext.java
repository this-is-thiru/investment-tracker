package com.thiru.wealthlens.dto.context;

import com.thiru.wealthlens.dto.enums.BrokerChargeTransactionType;
import com.thiru.wealthlens.dto.enums.BrokerName;
import com.thiru.wealthlens.dto.enums.CorporateActionType;

import java.time.LocalDate;

public record BrokerChargeContext(
        String transactionId,
        String stockCode,
        BrokerName brokerName,
        BrokerChargeTransactionType transactionType,
        LocalDate transactionDate,
        String exchangeName,
        CorporateActionType corporateActionType,
        double totalAmount
) {
}
