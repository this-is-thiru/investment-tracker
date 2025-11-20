package com.thiru.investment_tracker.entity.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class YearlyBrokerCharges extends BrokerChargesReport {
    @Field("monthly_report")
    private Map<Month, MonthlyBrokerCharges> monthlyReport = new HashMap<>();
}
