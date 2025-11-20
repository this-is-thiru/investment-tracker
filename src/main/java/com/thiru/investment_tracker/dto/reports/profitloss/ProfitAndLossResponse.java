package com.thiru.investment_tracker.dto.reports.profitloss;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.dto.model.AuditMetadataDto;
import com.thiru.investment_tracker.util.time.TLocalDateTime;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProfitAndLossResponse {

    private String email;
    private String financialYear;
    private RealisedProfitsResponse realisedProfits;
    private RealisedProfitsResponse outSourcedRealisedProfits;
    private double unrealisedProfit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    private LocalDateTime lastUpdatedTime;

    private AuditMetadataDto auditMetadata;
}
