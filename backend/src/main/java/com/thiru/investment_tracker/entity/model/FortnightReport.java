package com.thiru.investment_tracker.entity.model;

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
