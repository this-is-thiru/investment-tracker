package com.thiru.investment_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.dto.enums.InsuranceType;
import com.thiru.investment_tracker.dto.enums.PolicyType;
import com.thiru.investment_tracker.entity.model.PolicyDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Document(value = "insurances")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InsuranceEntity {

	@JsonIgnore
	@MongoId
	private String id;

	@Field("email")
	private String email;
	@Field("port_from")
	private String portFrom;
	@Field("port_to")
	private String portTo;
	@Field("insurance_id")
	private String insuranceId;
	@Field("insurance_type")
	private InsuranceType insuranceType;
	@Field("policy_type")
	private PolicyType policyType;
	@Field("premium")
	private double premium;
	@Field("start_date")
	private String startDate;
	@Field("end_date")
	private String endDate;
	@Field("maturity_date")
	private String maturityDate;
	@Field("renewal_date")
	private String renewalDate;
	@Field("maturity_amount")
	private String maturityAmount;
	@Field("broker")
	private String broker;
	@Field("agent_name")
	private String agentName;
	@Field("agent_email")
	private String agentEmail;
	@Field("agent_contact")
	private String agentContact;
	@Field("agent_address")
	private String agentAddress;
	@Field("insurance_status")
	private String insuranceStatus;
	@Field("notes")
	private String notes;

	private List<PolicyDetails> policyDetails;
}
