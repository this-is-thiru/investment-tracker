package com.thiru.investment_tracker.entity.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Month;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(staticName = "empty")
@Data
public class FinancialReport extends ReportModel {

	/**
	 * This stores P&L for each month (i.e each fortnight)
	 */
	private Map<Month, MonthlyReport> monthlyReport = new HashMap<>();
}
