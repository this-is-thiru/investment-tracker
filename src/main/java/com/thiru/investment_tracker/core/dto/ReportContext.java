package com.thiru.investment_tracker.core.dto;

import java.time.LocalDate;

import com.thiru.investment_tracker.core.dto.enums.AccountType;
import com.thiru.investment_tracker.core.dto.enums.AssetType;

import com.thiru.investment_tracker.core.dto.enums.BrokerName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "from")
@EqualsAndHashCode
@NoArgsConstructor(staticName = "empty")
public class ReportContext {
	private double purchasePrice;
	private LocalDate purchaseDate;
	private double sellPrice;
	private double sellQuantity;
	private LocalDate sellDate;
	private String stockCode;
	private String stockName;
	private String exchangeName;
	private BrokerName brokerName;
	private double totalValue;
	private AssetType assetType;
	private AccountType accountType;
	private String accountHolder;
}
