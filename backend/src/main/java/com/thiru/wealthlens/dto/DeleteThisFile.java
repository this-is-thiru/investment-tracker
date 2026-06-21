package com.thiru.wealthlens.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteThisFile {
	private String email;
	private String financialYear;
	private boolean isProfit;
	private double realisedProfit;
	private CapitalGains capitalGains;
	private double unrealisedProfit;

	@Data
	static class CapitalGains implements Serializable {
		private double shortTermCapitalGains;
		private double longTermCapitalGains;
	}
}
