package com.thiru.investment_tracker.core.entity.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "from")
@Data
public class FortnightReport {
	private double purchasePrice;
	private double sellPrice;
	private double profit;
	private double brokerCharges;
	private double miscCharges;
}
