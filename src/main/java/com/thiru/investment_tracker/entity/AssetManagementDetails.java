package com.thiru.investment_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.dto.enums.AmcChargeFrequency;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.entity.model.AuditableEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(value = "asset_management_details")
public class AssetManagementDetails implements AuditableEntity {
    @JsonIgnore
    @MongoId
    private String id;

    @Field("email")
    private String email;

    @Field("demat_account_id")
    private String dematAccountId;

    @Field(name = "broker_name", targetType = FieldType.STRING)
    private BrokerName brokerName;

    @Field("account_opening_charges")
    private double accountOpeningCharges;

    @Field("tax_on_account_opening_charges")
    private double taxOnAccountOpeningCharges;

    @Field("amcCharges")
    private double amcCharges;

    @Field("tax_on_amc_charges")
    private double taxOnAmcCharges;

    @Field("last_amc_charges_deducted_on")
    private LocalDate lastAmcChargesDeductedOn;

    @Field(name = "amc_charges_frequency", targetType = FieldType.STRING)
    private AmcChargeFrequency amcChargesFrequency;

    @Field("amc_charges_events")
    private List<AmcChargesEvent> amcChargesEvents = new ArrayList<>();

    @Field("audit_metadata")
    @Setter(value = AccessLevel.NONE)
    private AuditMetadata auditMetadata = new AuditMetadata();

    public record AmcChargesEvent(String userChargesId, LocalDate deductionDate, double deductionAmount, List<LocalDate> datesRange) {
    }
}
