package com.thiru.investment_tracker.entity;

import java.io.Serializable;

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
}