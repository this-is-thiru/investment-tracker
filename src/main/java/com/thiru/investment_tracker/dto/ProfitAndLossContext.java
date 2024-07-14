package com.thiru.investment_tracker.dto;

import java.time.LocalDate;

import com.thiru.investment_tracker.entity.Asset;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "from")
@EqualsAndHashCode
@Data
public class ProfitAndLossContext {
	private AssetContext purchaseContext;
	private AssetContext sellContext;
	private AssetMetadata metadata;

	/**
	 * Profit and loss context while selling the asset
	 */
	public static ProfitAndLossContext from(Asset asset, AssetRequest assetRequest, long sellQuantity) {
		double purchasePrice = asset.getPrice();
		LocalDate purchaseDate = asset.getTransactionDate();

		double purchaseBrokerCharge = (asset.getBrokerCharges() / asset.getQuantity()) * sellQuantity;
		double purchaseMiscCharge = (asset.getMiscCharges() / asset.getQuantity()) * sellQuantity;

		AssetContext purchaseContext = AssetContext.from();
		purchaseContext.setPrice(purchasePrice);
		purchaseContext.setQuantity(asset.getQuantity());
		purchaseContext.setTransactionDate(purchaseDate);
		purchaseContext.setAssetType(asset.getAssetType());
		purchaseContext.setBrokerCharges(purchaseBrokerCharge);
		purchaseContext.setMiscCharges(purchaseMiscCharge);

		double sellBrokerCharge = (assetRequest.getBrokerCharges() / assetRequest.getQuantity()) * sellQuantity;
		double sellMiscCharge = (assetRequest.getMiscCharges() / assetRequest.getQuantity()) * sellQuantity;

		AssetContext sellContext = AssetContext.from();
		sellContext.setPrice(assetRequest.getPrice());
		sellContext.setQuantity(sellQuantity);
		sellContext.setTransactionDate(assetRequest.getTransactionDate());
		sellContext.setAssetType(assetRequest.getAssetType());
		sellContext.setBrokerCharges(sellBrokerCharge);
		sellContext.setMiscCharges(sellMiscCharge);

		AssetMetadata metadata = AssetMetadata.from();
		metadata.setAccountType(assetRequest.getAccountType());
		metadata.setAccountHolder(assetRequest.getAccountHolder());

		ProfitAndLossContext profitAndLossContext = ProfitAndLossContext.from();
		profitAndLossContext.setPurchaseContext(purchaseContext);
		profitAndLossContext.setSellContext(sellContext);
		profitAndLossContext.setMetadata(metadata);
		return profitAndLossContext;
	}
}
