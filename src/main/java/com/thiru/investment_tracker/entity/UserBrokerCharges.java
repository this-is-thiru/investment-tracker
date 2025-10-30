package com.thiru.investment_tracker.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.dto.enums.BrokerChargeTransactionType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.entity.model.AuditableEntity;
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


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "user_broker_charges")
public class UserBrokerCharges implements AuditableEntity {
    @JsonIgnore
    @MongoId
    private String id;

    @Field("email")
    private String email;

    @Field(name = "broker_name", targetType = FieldType.STRING)
    private BrokerName brokerName;

    @Field("stock_code")
    private String stockCode;

    @Field("transaction_date")
    private LocalDate transactionDate;

    @Field(name = "type", targetType = FieldType.STRING)
    private BrokerChargeTransactionType type;

    @Field("transaction_id")
    private String transactionId;

    @Field("brokerage")
    private double brokerage;

    @Field("account_opening_charges")
    private double accountOpeningCharges;

    @Field("amc_charges")
    private double amcCharges;

    @Field("govt_charges")
    private double govtCharges;

    @Field("taxes")
    private double taxes;

    @Field("dp_charges")
    private double dpCharges;

    @Field("audit_metadata")
    @Setter(value = AccessLevel.NONE)
    private AuditMetadata auditMetadata = new AuditMetadata();
}
