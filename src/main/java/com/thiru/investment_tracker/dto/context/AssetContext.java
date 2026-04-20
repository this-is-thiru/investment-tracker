package com.thiru.investment_tracker.dto.context;

import java.time.LocalDate;

import com.thiru.investment_tracker.dto.enums.AssetType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(staticName = "from")
public class AssetContext {
	private String name;
	private double price;
	private double quantity;
	private LocalDate transactionDate;
	private AssetType assetType;
	private double brokerCharges;
	private double miscCharges;
}
