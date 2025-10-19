package com.thiru.investment_tracker.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.entity.model.AuditableEntity;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;

@Document(value = "reports")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportEntity implements AuditableEntity {
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

	@Field("purchase_price")
	private double purchasePrice;

	@Field("sell_price")
	private double sellPrice;

	@Field("quantity")
	private Long sellQuantity;

	@Field("total_value")
	private double totalValue;

	@Field(name = "asset_type", targetType = FieldType.STRING)
	private AssetType assetType;

	@Field(name = "account_type", targetType = FieldType.STRING)
	private AccountType accountType;

	@Field("account_holder")
	private String accountHolder;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	@Field("purchase_date")
	private LocalDate purchaseDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	@Field("sell_date")
	private LocalDate sellDate;

	@Field("audit_metadata")
	@Setter(value = AccessLevel.NONE)
	private AuditMetadata auditMetadata = new AuditMetadata();

}
