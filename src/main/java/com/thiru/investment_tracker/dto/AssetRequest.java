package com.thiru.investment_tracker.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.TransactionType;

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
	private BrokerName brokerName;
	private AssetType assetType;
	private Double price;
	private Double quantity;
	private TransactionType transactionType;
	private AccountType accountType = AccountType.SELF;
	private String accountHolder;
	private String actor;
	private double brokerCharges;
	private double miscCharges;
	private String comment;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	private LocalDate maturityDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	private LocalDate transactionDate;

}
