package com.thiru.investment_tracker.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Document(value = "profit_and_loss")
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class ProfitAndLossEntity {

	@JsonIgnore
	@MongoId
	private String id;

	@Field("email")
	private String email;

	@Field("financial_year")
	private String financialYear;

	@Field("is_profit")
	private boolean isProfit;

	@Field("realised_profits")
	private RealisedProfits realisedProfits;

	@Field("out_sourced_realised_profits")
	private RealisedProfits outSourcedRealisedProfits;

	@Field("unrealised_profit")
	private double unrealisedProfit;

	@Field("last_updated_time")
	private LocalDateTime lastUpdatedTime;

	public ProfitAndLossEntity(String email) {
		this.email = email;
	}

}
