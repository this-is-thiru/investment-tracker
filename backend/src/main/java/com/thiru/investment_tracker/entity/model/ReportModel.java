package com.thiru.investment_tracker.entity.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
public class ReportModel {
    @Field("purchase_price")
    private double purchasePrice;

    @Field("sell_price")
    private double sellPrice;

    @Field("profit")
    private double profit;

    @Field("broker_charges")
    private double brokerCharges;

    @Field("misc_charges")
    private double miscCharges;

    @Field(name = "last_updated_time")
    private LocalDateTime lastUpdatedTime;
}
