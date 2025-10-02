package com.thiru.investment_tracker.dto.context;

import com.thiru.investment_tracker.dto.enums.BrokerChargeTransactionType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;

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
