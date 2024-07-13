package com.thiru.investment_tracker.entity;

import java.time.Month;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MonthlyReport {
	private Month month;
	private FortnightReport firstFortnightReport;
	private FortnightReport secondFortnightReport;

	public MonthlyReport(Month month) {
		this.month = month;
	}
}
