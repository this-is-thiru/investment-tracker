package com.thiru.investment_tracker.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Data
public class RealisedProfits implements Serializable {
    @Field("total_realised_profit")
    private double totalRealisedProfit;

    @Field("short_term_capital_gains")
    private double shortTermCapitalGains;

    @Field("long_term_capital_gains")
    private double longTermCapitalGains;
}