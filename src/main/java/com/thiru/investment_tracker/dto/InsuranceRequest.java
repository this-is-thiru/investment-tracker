package com.thiru.investment_tracker.dto;

import java.util.List;

import com.thiru.investment_tracker.common.Enums.InsuranceType;
import com.thiru.investment_tracker.common.Enums.PolicyType;

import lombok.Data;

@Data
public class InsuranceRequest {

	private String email;
	private String insuranceId;
	private String insurancePolicy;
	private InsuranceType insuranceType;
	private PolicyType policyType;

	private String startDate;
	private String endDate;
	private String maturityDate;
	private String renewalDate;
	private String maturityAmount;

	private double premium;

	private String broker;
	private String agent;
	private String agentName;
	private String agentEmail;
	private String agentContact;
	private String agentAddress;

	private String insuranceStatus;
	private String notes;

	private List<String> persons;
}
