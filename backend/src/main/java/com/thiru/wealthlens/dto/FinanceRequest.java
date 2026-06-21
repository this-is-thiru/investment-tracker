package com.thiru.wealthlens.dto;

import com.thiru.wealthlens.dto.enums.CalculationType;
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
