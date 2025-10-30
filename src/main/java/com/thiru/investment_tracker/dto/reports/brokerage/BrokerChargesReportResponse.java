package com.thiru.investment_tracker.dto.reports.brokerage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.util.time.TLocalDateTime;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrokerChargesReportResponse {
    private double brokerage;
    private double accountOpeningCharges;
    private double amcCharges;
    private double govtCharges;
    private double taxes;
    private double dpCharges;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    private LocalDateTime lastUpdatedTime;
}
