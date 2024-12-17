package com.thiru.investment_tracker.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.TransactionType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AssetResponse {
	private String email;
	private String stockCode;
	private String stockName;
	private String exchangeName;
	private BrokerName brokerName;
	private AssetType assetType;
	private LocalDate maturityDate;
	private double price;
	private Double quantity;
	private Double totalQuantity;
	private double totalValue;
	private TransactionType transactionType;
	private String actor;
	private String accountType;
	private String accountHolder;
	private String remarks;
	private double brokerCharges;
	private double miscCharges;
	private String comment;
	private List<String> buyTransactionIds = new ArrayList<>();
	private List<String> sellTransactionIds = new ArrayList<>();
	List<CorporateActionWrapper> corporateActions = new ArrayList<>();

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	private LocalDate transactionDate;

	private Map<String, Double> transactionQuantities;
}
