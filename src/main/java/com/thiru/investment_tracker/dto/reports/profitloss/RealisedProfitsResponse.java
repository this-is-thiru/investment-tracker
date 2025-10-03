package com.thiru.investment_tracker.dto.reports.profitloss;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.dto.reports.brokerage.YearlyBrokerChargesResponse;
import com.thiru.investment_tracker.util.time.TLocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class RealisedProfitsResponse {

	private FinancialReportResponse shortTermCapitalGains;
	private FinancialReportResponse longTermCapitalGains;
    private YearlyBrokerChargesResponse yearlyBrokerCharges;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
	private LocalDateTime lastUpdatedTime;

}
