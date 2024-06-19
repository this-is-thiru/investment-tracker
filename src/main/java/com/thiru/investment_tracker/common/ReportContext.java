package com.thiru.investment_tracker.common;

import java.time.LocalDate;

import com.thiru.investment_tracker.common.enums.AssetType;

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
	private long sellQuantity;
	private LocalDate sellDate;
	private String stockCode;
	private String stockName;
	private String exchangeName;
	private String brokerName;
	private double totalValue;
	private AssetType assetType;
	private String actor;
}
