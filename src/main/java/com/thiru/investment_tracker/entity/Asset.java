package com.thiru.investment_tracker.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.dto.CorporateActionWrapper;
import com.thiru.investment_tracker.dto.OrderTimeQuantity;
import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.collection.TLocaleDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

	@Field(name = "broker_name", targetType = FieldType.STRING)
	private BrokerName brokerName;

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

	@JsonFormat(pattern = TCollectionUtil.DATE_FORMAT)
	@Field("maturity_date")
	private LocalDate maturityDate;

	@Field("actor_name")
	private String actor;

	@JsonFormat(pattern = TCollectionUtil.DATE_FORMAT)
	@Field("transaction_date")
	private LocalDate transactionDate;

	@Field("order_id")
	private String orderId;

	// remove this as we use orderTimeQuantity
	@Field("order_execution_time")
	private Instant orderExecutionTime;

	@Field("order_time_quantities")
	private List<OrderTimeQuantity> orderTimeQuantities = new ArrayList<>();

	@Field("timezone_id")
	private String timezoneId = TLocaleDate.TIME_ZONE_IST;

	@Field(name = "account_type", targetType = FieldType.STRING)
	private AccountType accountType;

	@Field("account_holder")
	private String accountHolder;

	@JsonIgnore
	@Field(name = "transaction_type", targetType = FieldType.STRING)
	private TransactionType transactionType;

	@Field("comments")
	private String comment;

	@Field("buy_transaction_ids")
	private List<String> buyTransactionIds = new ArrayList<>();

	@Field("sell_transaction_ids")
	private List<String> sellTransactionIds = new ArrayList<>();

	@Field("corporate_actions")
	List<CorporateActionWrapper> corporateActions = new ArrayList<>();

	// this email we can't accept from the request payload, we are formatting this through the code
	public static final String EMAIL = "email";

	public static Set<String> ALLOWED_FIELDS = Set.of("email", "transaction_date", "transaction_type",
			"account_type", "account_holder", "exchange_name", "stock_code", "broker_name", "asset_type");

}
