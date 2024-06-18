package com.thiru.investment_tracker.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.common.TCommonUtil;
import com.thiru.investment_tracker.common.enums.AssetType;
import com.thiru.investment_tracker.common.enums.TransactionType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AssetRequest {
	private String email;
	private String stockCode;
	private String stockName;
	private String exchangeName;
	private String brokerName;
	private AssetType assetType;
	private Double price;
	private Long quantity;
	private TransactionType transactionType;
	private String actor;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCommonUtil.DATE_FORMAT)
	private LocalDate maturityDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCommonUtil.DATE_FORMAT)
	private LocalDate transactionDate;

}
