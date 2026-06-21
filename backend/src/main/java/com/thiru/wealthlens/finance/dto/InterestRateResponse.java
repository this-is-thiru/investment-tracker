package com.thiru.wealthlens.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestRateResponse {
    private double monthlyRate;
    private double annualRate;
}
