package com.thiru.investment_tracker.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(staticName = "empty")
public class RealisedProfits implements Serializable {
	@Field("total_realised_profit")
	private double totalRealisedProfit;

	@Field("short_term_capital_gains")
	private double shortTermCapitalGains;

	@Field("long_term_capital_gains")
	private double longTermCapitalGains;

	@Field("broker_charges")
	private double totalBrokerCharges;

	@Field("misc_charges")
	private double totalMiscCharges;

	@Field("last_updated_time")
	private LocalDateTime lastUpdatedTime;

	/**
	 * This stores P&L for each month (i.e each fortnight)
	 */
	@Field("monthly_reports")
	private Map<Month, MonthlyReport> monthlyReports = new HashMap<>();
}