package com.thiru.investment_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.entity.RealisedProfits;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProfitAndLossResponse {
	private String email;
	private String financialYear;
	private boolean isProfit;
	private RealisedProfits realisedProfits;
	private RealisedProfits outSourcedRealisedProfits;
	private double unrealisedProfit;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastUpdatedTime;
}
