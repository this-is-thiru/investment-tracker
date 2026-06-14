package com.thiru.investment_tracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XirrResponse {
    private Double xirr;
    private Double xirrPercentage;
    private LocalDate calculationDate;
    private Integer cashFlowsCount;
    private Boolean converged;
    private Boolean includesOpenPositions;
    private String message;
}