package com.thiru.investment_tracker.dto;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thiru.investment_tracker.entity.MonthlyReport;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(staticName = "empty")
public class CapitalGainsResponse {
    private Map<Month, MonthlyReport> shortTermMonthlyGain = new HashMap<>();
    private Map<Month, MonthlyReport> longTermMonthlyGain = new HashMap<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdatedTime;
}
