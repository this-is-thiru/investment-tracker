package com.thiru.investment_tracker.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.dto.enums.AssetType;

import com.thiru.investment_tracker.util.collection.TLocaleDate;
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
	private BrokerName brokerName;
	private AssetType assetType;
	private double price;
	private Double quantity;
	private double totalValue;
	private TransactionType transactionType;
	private String orderId;
	private Instant orderExecutionTime;
	private String timezoneId = TLocaleDate.TIME_ZONE_IST;
	private String accountType;
	private String accountHolder;
	private double brokerCharges;
	private double miscCharges;
	private String comment;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	private LocalDate maturityDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	private LocalDate transactionDate;

	List<CorporateActionDto> corporateActions = new ArrayList<>();
}