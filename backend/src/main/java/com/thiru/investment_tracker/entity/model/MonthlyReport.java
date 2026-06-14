package com.thiru.investment_tracker.entity.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Month;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MonthlyReport extends ReportModel {
    private Month month;

    @Field("first_half_report")
    private FortnightReport firstFortnightReport;

    @Field("second_half_report")
    private FortnightReport secondFortnightReport;

    public MonthlyReport(Month month) {
        this.month = month;
    }
}
