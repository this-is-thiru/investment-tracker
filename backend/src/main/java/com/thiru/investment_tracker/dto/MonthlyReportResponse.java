package com.thiru.investment_tracker.dto;

import java.time.Month;

import com.thiru.investment_tracker.entity.model.FortnightReport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MonthlyReportResponse extends ReportModelResponse {
	private Month month;
	private FortnightReport firstFortnightReport;
	private FortnightReport secondFortnightReport;
}
