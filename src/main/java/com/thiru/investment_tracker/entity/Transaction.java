package com.thiru.investment_tracker.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.dto.CorporateActionWrapper;
import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.collection.TLocaleDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(value = "transactions")
@AllArgsConstructor
@NoArgsConstructor
@Data
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

	@Field(name = "broker_name", targetType = FieldType.STRING)
	private BrokerName brokerName;

	@Field("price")
	private double price;

	@Field("quantity")
	private Double quantity;

	@Field("total_value")
	private double totalValue;

	@Field("broker_charges")
	private double brokerCharges;

	@Field("misc_charges")
	private double miscCharges;

	@Field("comment")
	private String comment;

	@Field(name = "asset_type", targetType = FieldType.STRING)
	private AssetType assetType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	@Field("maturity_date")
	private LocalDate maturityDate;

	@Field("order_id")
	private String orderId;

	@Field("order_execution_time")
	private Instant orderExecutionTime;

	@Field("timezone_id")
	private String timezoneId = TLocaleDate.TIME_ZONE_IST;

	@Field(name = "account_type", targetType = FieldType.STRING)
	private AccountType accountType;

	@Field("account_holder")
	private String accountHolder;

	@Field("actor_name")
	private String actor;

	@Field(name = "transaction_type", targetType = FieldType.STRING)
	private TransactionType transactionType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	@Field("transaction_date")
	private LocalDate transactionDate;

	@Field("corporate_actions")
	List<CorporateActionWrapper> corporateActions = new ArrayList<>();

}
