package com.thiru.investment_tracker.entity.model;

import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
public class ReportModel {
    @Field("purchase_amount")
    private double purchaseAmount;

    @Field("sell_amount")
    private double sellAmount;

    @Field("profit")
    private double profit;

    @Field("misc_charges")
    private double miscCharges;

    @Field("brokerage")
    private double brokerage;

    @Field(name = "last_updated_time")
    @LastModifiedDate
    private LocalDateTime lastUpdatedTime;
}
