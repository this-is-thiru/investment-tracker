package com.thiru.investment_tracker.dto.reports.profitloss;

import com.thiru.investment_tracker.dto.context.BuyContext;
import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.dto.enums.TransactionType;

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
        TransactionType transactionType,
        CorporateActionType actionType,
        AccountType accountType,
        String accountHolder,
        List<BuyContext> buyContexts
) {
}
