package com.thiru.investment_tracker.core.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RealisedProfitsResponse {
	private FinancialReportResponse shortTermCapitalGains;
	private FinancialReportResponse longTermCapitalGains;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastUpdatedTime;

}
