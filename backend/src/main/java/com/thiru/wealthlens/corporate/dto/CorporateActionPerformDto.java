package com.thiru.wealthlens.corporate.dto;

import com.thiru.wealthlens.portfolio.dto.enums.BrokerName;

public record CorporateActionPerformDto(
        String month,
        int year,
        BrokerName brokerName
) {
}
