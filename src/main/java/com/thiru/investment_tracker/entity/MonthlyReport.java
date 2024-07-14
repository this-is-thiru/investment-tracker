package com.thiru.investment_tracker.entity;

import java.time.Month;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MonthlyReport extends ReportModel {
	private Month month;

	@Field("first_half_report")
	private FortnightReport firstFortnightReport;

	@Field("second_half_report")
	private FortnightReport secondFortnightReport;

	public MonthlyReport(Month month) {
		this.month = month;
	}
}
