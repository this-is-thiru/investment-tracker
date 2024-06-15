package com.thiru.investment_tracker.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.common.CommonUtil;
import com.thiru.investment_tracker.common.enums.AssetType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransactionResponse {
	private String email;
	private String stockCode;
	private String stockName;
	private String exchangeName;
	private String brokerName;
	private double price;
	private Long quantity;
	private double totalValue;
	private String actorName;
	private AssetType assetType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonUtil.DATE_FORMAT)
	private LocalDate maturityDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonUtil.DATE_FORMAT)
	private LocalDate transactionDate;
}