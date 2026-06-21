package com.thiru.wealthlens.dto.context;

import java.time.LocalDate;

import com.thiru.wealthlens.dto.enums.AssetType;

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
