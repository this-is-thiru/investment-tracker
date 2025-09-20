package com.thiru.investment_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceResponse {
    private double principalAmount;
    private double rateOfInterest;
    private int timePeriod;
    private double finalAmount;
    private int emiAmount;
}