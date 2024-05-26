package com.thiru.investment_tracker.dto;

import java.util.List;

import com.thiru.investment_tracker.common.Enums.InsuranceType;
import com.thiru.investment_tracker.common.Enums.PolicyType;
import com.thiru.investment_tracker.entity.PolicyDetails;

import lombok.Data;

@Data
public class InsuranceResponse {

	private String email;
	private String portFrom;
	private String portTo;
	private String insuranceId;
	private String insurancePolicy;
	private InsuranceType insuranceType;
	private PolicyType policyType;
	private double premium;
	private String startDate;
	private String endDate;
	private String maturityDate;
	private String renewalDate;
	private String maturityAmount;
	private String broker;
	private String agentName;
	private String agentEmail;
	private String agentContact;
	private String agentAddress;
	private String insuranceStatus;
	private String notes;

	private List<PolicyDetails> policyDetails;
}
