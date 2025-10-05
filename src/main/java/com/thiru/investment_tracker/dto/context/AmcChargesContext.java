package com.thiru.investment_tracker.dto.context;

import com.thiru.investment_tracker.dto.enums.BrokerChargeTransactionType;
import com.thiru.investment_tracker.dto.enums.BrokerName;

import java.time.LocalDate;

public record AmcChargesContext(
        BrokerName brokerName,
        BrokerChargeTransactionType transactionType,
        LocalDate transactionDate,
        double amount,
        double taxes
) {
}
