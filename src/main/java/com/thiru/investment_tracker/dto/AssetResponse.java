package com.thiru.investment_tracker.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.common.CommonUtil;
import com.thiru.investment_tracker.common.Enums.AssetType;
import com.thiru.investment_tracker.common.Enums.TransactionType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AssetResponse {
	private String email;
	private String stockCode;
	private String stockName;
	private String exchangeName;
	private String brokerName;
	private AssetType assetType;
	private Date maturityDate;
	private double price;
	private Long quantity;
	private double totalValue;
	private TransactionType transactionType;
	private String actorName;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonUtil.DATE_FORMAT)
	private Date transactionDate;

}
