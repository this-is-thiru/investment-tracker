package com.thiru.investment_tracker.dto.reports.profitloss;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.util.time.TLocalDateTime;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportModelResponse {
    private double purchaseAmount;
    private double sellAmount;
    private double profit;
    private double miscCharges;
    private double brokerage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    private LocalDateTime lastUpdatedTime;
}
