package com.thiru.investment_tracker.dto.reports.brokerage;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Month;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class YearlyBrokerChargesResponse extends BrokerChargesReportResponse {
    private Map<Month, MonthlyBrokerChargesResponse> monthlyReport = new HashMap<>();
}
