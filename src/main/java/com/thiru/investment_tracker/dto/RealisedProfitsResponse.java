package com.thiru.investment_tracker.dto;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thiru.investment_tracker.entity.MonthlyReport;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RealisedProfitsResponse {
	private double totalRealisedProfit;
	private double shortTermCapitalGains;
	private double longTermCapitalGains;
	private double totalBrokerCharges;
	private double totalMiscCharges;
	private Map<Month, MonthlyReport> monthlyReports;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastUpdatedTime;
}
