package com.thiru.investment_tracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlow {
    private Double amount;
    private LocalDate date;
}