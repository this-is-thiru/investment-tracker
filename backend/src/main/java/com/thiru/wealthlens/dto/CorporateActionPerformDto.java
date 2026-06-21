package com.thiru.wealthlens.dto;

import com.thiru.wealthlens.dto.enums.BrokerName;

public record CorporateActionPerformDto(
        String month,
        int year,
        BrokerName brokerName
) {
}
