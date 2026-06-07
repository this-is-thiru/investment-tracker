package com.thiru.investment_tracker.dto;

import com.thiru.investment_tracker.dto.enums.BrokerName;

public record CorporateActionPerformDto(
        String month,
        int year,
        BrokerName brokerName
) {
}
