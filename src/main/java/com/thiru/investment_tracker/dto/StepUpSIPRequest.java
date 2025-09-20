package com.thiru.investment_tracker.dto;

import com.thiru.investment_tracker.dto.enums.StepUpFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepUpSIPRequest {
    private double initialAmount;
    private double stepUpRate;
    private StepUpFrequency stepUpFrequency;
    private int months;
    private double targetAmount;
}
