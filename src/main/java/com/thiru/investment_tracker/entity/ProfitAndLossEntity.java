package com.thiru.investment_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.entity.model.AuditableEntity;
import com.thiru.investment_tracker.entity.model.RealisedProfits;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document(value = "profit_and_loss")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfitAndLossEntity implements AuditableEntity {

	@JsonIgnore
	@MongoId
	private String id;

	@Field("email")
	private String email;

	@Field("financial_year")
	private String financialYear;

	@Field("realised_profits")
	private RealisedProfits realisedProfits;

	@Field("out_sourced_realised_profits")
	private RealisedProfits outSourcedRealisedProfits;

	@Field("unrealised_profit")
	private double unrealisedProfit;

	@Field("last_updated_time")
	private LocalDateTime lastUpdatedTime;

	@Field("audit_metadata")
	@Setter(value = AccessLevel.NONE)
	private AuditMetadata auditMetadata = new AuditMetadata();

	public ProfitAndLossEntity(String email) {
		this.email = email;
	}

    public ProfitAndLossEntity(String email, String financialYear) {
        this.email = email;
        this.financialYear = financialYear;
    }

}
