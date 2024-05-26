package com.thiru.investment_tracker.dto;

import com.thiru.investment_tracker.entity.RealisedProfits;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfitAndLossResponse {
	private String email;
	private String financialYear;
	private boolean isProfit;
	private RealisedProfits realisedProfits;
	private double unrealisedProfit;
}
