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
public class MonthlyBrokerCharges extends BrokerChargesReport {
    private Month month;

    @Field("first_half_broker_charges")
    private BrokerChargesReport firstHalfBrokerCharges;

    @Field("second_half_broker_charges")
    private BrokerChargesReport secondHalfBrokerCharges;

    public MonthlyBrokerCharges(Month month) {
        this.month = month;
    }
}
