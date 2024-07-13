package com.thiru.investment_tracker.dto;

import java.time.LocalDate;

import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.entity.Asset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "from")
@NoArgsConstructor(staticName = "from")
@EqualsAndHashCode
@Data
public class ProfitAndLossContext {
	private double purchasePrice;
	private LocalDate purchaseDate;
	private double sellPrice;
	private long sellQuantity;
	private LocalDate sellDate;
	private AccountType accountType;
	private String accountHolder;
	private TransactionType transactionType;
	private double brokerCharges;
	private double miscCharges;

	/**
	 * Profit and loss context while selling the asset
	 */
	public static ProfitAndLossContext from(Asset asset, AssetRequest assetRequest, long sellQuantity) {
		double purchasePrice = asset.getPrice();
		LocalDate purchaseDate = asset.getTransactionDate();

		double sellPrice = assetRequest.getPrice();
		LocalDate sellDate = assetRequest.getTransactionDate();

		ProfitAndLossContext profitAndLossContext = ProfitAndLossContext.from();
		profitAndLossContext.setPurchasePrice(purchasePrice);
		profitAndLossContext.setPurchaseDate(purchaseDate);
		profitAndLossContext.setSellPrice(sellPrice);
		profitAndLossContext.setSellQuantity(sellQuantity);
		profitAndLossContext.setSellDate(sellDate);
		profitAndLossContext.setAccountType(assetRequest.getAccountType());
		profitAndLossContext.setAccountHolder(assetRequest.getAccountHolder());
		profitAndLossContext.setTransactionType(assetRequest.getTransactionType());
		profitAndLossContext.setBrokerCharges(assetRequest.getBrokerCharges());
		profitAndLossContext.setMiscCharges(assetRequest.getMiscCharges());

		return profitAndLossContext;
	}

	/**
	 * Profit and loss context while buying the asset
	 */
	public static ProfitAndLossContext from(AssetRequest assetRequest) {

		LocalDate purchaseDate = assetRequest.getTransactionDate();

		ProfitAndLossContext profitAndLossContext = ProfitAndLossContext.from();
		profitAndLossContext.setPurchaseDate(purchaseDate);
		profitAndLossContext.setAccountType(assetRequest.getAccountType());
		profitAndLossContext.setAccountHolder(assetRequest.getAccountHolder());
		profitAndLossContext.setTransactionType(assetRequest.getTransactionType());
		profitAndLossContext.setBrokerCharges(assetRequest.getBrokerCharges());
		profitAndLossContext.setMiscCharges(assetRequest.getMiscCharges());

		return profitAndLossContext;
	}
}
