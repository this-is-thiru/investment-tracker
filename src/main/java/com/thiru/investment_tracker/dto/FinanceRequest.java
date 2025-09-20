package com.thiru.investment_tracker.dto;

import com.thiru.investment_tracker.dto.enums.CalculationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRequest {
    private CalculationType calculationType;
    private double principalAmount;
    private double rateOfInterest;
    private int timePeriod;
    private double finalAmount;
    private double emiAmount;
}