package com.thiru.investment_tracker.entity.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor(staticName = "empty")
public class RealisedProfits implements Serializable {

	@Field("last_updated_time")
	private LocalDateTime lastUpdatedTime;

	private FinancialReport shortTermCapitalGains;
	private FinancialReport longTermCapitalGains;
}
