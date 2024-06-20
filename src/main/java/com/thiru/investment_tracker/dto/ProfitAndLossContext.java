package com.thiru.investment_tracker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor(staticName = "from")
@EqualsAndHashCode
@Data
public class ProfitAndLossContext {
	private double purchasePrice;
	private LocalDate purchaseDate;
	private double sellPrice;
	private long sellQuantity;
	private LocalDate sellDate;
}
