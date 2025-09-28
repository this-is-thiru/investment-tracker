package com.thiru.investment_tracker.core.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thiru.investment_tracker.core.dto.model.AuditMetadataDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfitAndLossResponse {
	private String email;
	private String financialYear;
	private boolean isProfit;
	private RealisedProfitsResponse realisedProfits;
	private RealisedProfitsResponse outSourcedRealisedProfits;
	private double unrealisedProfit;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastUpdatedTime;

	private AuditMetadataDto auditMetadata;
}
