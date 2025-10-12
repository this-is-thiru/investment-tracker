package com.thiru.investment_tracker.core.entity.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(staticName = "empty")
public class RealisedProfits implements Serializable {

	@Field("last_updated_time")
	private LocalDateTime lastUpdatedTime;

	private FinancialReport shortTermCapitalGains;
	private FinancialReport longTermCapitalGains;
}
