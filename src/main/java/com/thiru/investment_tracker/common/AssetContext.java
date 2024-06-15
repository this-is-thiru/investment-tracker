package com.thiru.investment_tracker.common;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AssetContext {
	private double purchasePrice;
	private LocalDate purchaseDate;
	private double sellPrice;
	private long sellQuantity;
	private LocalDate sellDate;

	public static AssetContext from(double purchasePrice, LocalDate purchaseDate, double sellPrice, long sellQuantity,
			LocalDate sellDate) {
		AssetContext assetContext = new AssetContext();
		assetContext.setPurchasePrice(purchasePrice);
		assetContext.setPurchaseDate(purchaseDate);
		assetContext.setSellPrice(sellPrice);
		assetContext.setSellQuantity(sellQuantity);
		assetContext.setSellDate(sellDate);
		return assetContext;
	}
}
