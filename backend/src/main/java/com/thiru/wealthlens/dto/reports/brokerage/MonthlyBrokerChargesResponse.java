package com.thiru.wealthlens.dto.reports.brokerage;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Month;

@Data
@EqualsAndHashCode(callSuper = true)
public class MonthlyBrokerChargesResponse extends BrokerChargesReportResponse {
    private Month month;
    private BrokerChargesReportResponse firstHalfBrokerCharges;
    private BrokerChargesReportResponse secondHalfBrokerCharges;
}
