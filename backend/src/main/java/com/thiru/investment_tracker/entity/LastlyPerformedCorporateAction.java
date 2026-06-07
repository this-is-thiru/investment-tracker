package com.thiru.investment_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.entity.model.AuditableEntity;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(value = "lastly-performed-corporate-action")
public class LastlyPerformedCorporateAction implements AuditableEntity {
    @JsonIgnore
    @MongoId
    private String id;

    private String email;

    @Field("stock_code")
    private String stockCode;

    @Field("asset_type")
    private AssetType assetType;

    @Field("action_type")
    private CorporateActionType actionType;

    @Field("action_date")
    private LocalDate actionDate;

    @Field("broker_name")
    private BrokerName brokerName;

    @Field("audit_metadata")
    @Setter(value = AccessLevel.NONE)
    private final AuditMetadata auditMetadata = new AuditMetadata();
}
