package com.thiru.investment_tracker.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.core.dto.enums.AssetType;
import com.thiru.investment_tracker.core.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.core.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.core.entity.model.AuditableEntity;
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
    private String stockCode;
    private AssetType assetType;
    private CorporateActionType actionType;
    private LocalDate actionDate;

    @Field("audit_metadata")
    @Setter(value = AccessLevel.NONE)
    private final AuditMetadata auditMetadata = new AuditMetadata();
}
