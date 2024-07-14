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

		double brokerCharges = asset.getBrokerCharges() / asset.getQuantity() * sellQuantity;
		double miscCharges = asset.getMiscCharges() / asset.getQuantity() * sellQuantity;

		AssetContext purchaseContext = AssetContext.from();
		purchaseContext.setPrice(purchasePrice);
		purchaseContext.setQuantity(asset.getQuantity());
		purchaseContext.setTransactionDate(purchaseDate);
		purchaseContext.setAssetType(asset.getAssetType());
		purchaseContext.setBrokerCharges(brokerCharges);
		purchaseContext.setMiscCharges(miscCharges);

		double sellPrice = assetRequest.getPrice();
		LocalDate sellDate = assetRequest.getTransactionDate();

		AssetContext sellContext = AssetContext.from();
		sellContext.setPrice(sellPrice);
		sellContext.setQuantity(sellQuantity);
		sellContext.setTransactionDate(sellDate);
		sellContext.setAssetType(assetRequest.getAssetType());
		sellContext.setBrokerCharges(assetRequest.getBrokerCharges());
		sellContext.setMiscCharges(assetRequest.getMiscCharges());

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
