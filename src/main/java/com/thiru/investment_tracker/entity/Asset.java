package com.thiru.investment_tracker.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Immutable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.thiru.investment_tracker.common.TCommonUtil;
import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document(value = "assets")
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class Asset {

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
	private Double quantity;

	@Field("total_value")
	private double totalValue;

	@Field(name = "asset_type", targetType = FieldType.STRING)
	private AssetType assetType;

	@Field("broker_charges")
	private double brokerCharges;

	@Field("misc_charges")
	private double miscCharges;

	@JsonFormat(pattern = TCommonUtil.DATE_FORMAT)
	@Field("maturity_date")
	private LocalDate maturityDate;

	@Field("actor_name")
	private String actor;

	@JsonFormat(pattern = TCommonUtil.DATE_FORMAT)
	@Field("transaction_date")
	private LocalDate transactionDate;

	@Field(name = "account_type", targetType = FieldType.STRING)
	private AccountType accountType;

	@Field("account_holder")
	private String accountHolder;

	@JsonIgnore
	@Field(name = "transaction_type", targetType = FieldType.STRING)
	private TransactionType transactionType;

	@Field("comments")
	private String comment;

	// this email we can't accept from the request payload, we are formatting this through the code
	public static final String EMAIL = "email";

	public static Set<String> ALLOWED_FIELDS = Set.of("email", "transaction_date", "transaction_type",
			"account_type", "account_holder", "exchange_name", "stock_code", "broker_name", "asset_type");

}
