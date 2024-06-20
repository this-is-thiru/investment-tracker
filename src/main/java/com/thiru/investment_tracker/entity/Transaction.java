package com.thiru.investment_tracker.entity;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.thiru.investment_tracker.common.TCommonUtil;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document(value = "transactions")
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class Transaction {

	@JsonIgnore
	@MongoId
	private String id;

	@Field("email")
	private String email;

	@Field("stock_code")
	private String stockCode;

	@Field("stock_name")
	private String stockName;

	@Field("exchange_name")
	private String exchangeName;

	@Field("broker_name")
	private String brokerName;

	@Field("price")
	private double price;

	@Field("quantity")
	private Long quantity;

	@Field("total_value")
	private double totalValue;

	@Field(name = "asset_type", targetType = FieldType.STRING)
	private AssetType assetType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCommonUtil.DATE_FORMAT)
	@Field("maturity_date")
	private LocalDate maturityDate;

	@Field("actor_name")
	private String actor;

	@Field(name = "transaction_type", targetType = FieldType.STRING)
	private TransactionType transactionType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCommonUtil.DATE_FORMAT)
	@Field("transaction_date")
	private LocalDate transactionDate;

}
