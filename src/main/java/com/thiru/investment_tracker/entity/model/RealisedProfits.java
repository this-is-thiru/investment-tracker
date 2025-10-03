package com.thiru.investment_tracker.entity.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor(staticName = "empty")
public class RealisedProfits implements Serializable {

	@Field("last_updated_time")
    @LastModifiedDate
	private LocalDateTime lastUpdatedTime;

    @Field("short_term_capital_gains")
	private FinancialReport shortTermCapitalGains;

    @Field("long_term_capital_gains")
	private FinancialReport longTermCapitalGains;

    @Field("yearly_broker_charges")
    private YearlyBrokerCharges yearlyBrokerCharges;
}
