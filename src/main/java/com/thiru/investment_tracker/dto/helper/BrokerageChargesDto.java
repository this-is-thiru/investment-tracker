package com.thiru.investment_tracker.dto.helper;


import com.thiru.investment_tracker.dto.enums.BrokerageAggregatorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerageChargesDto {
    private double brokerage;
    private double brokerageCharges;
    private BrokerageAggregatorType brokerageAggregator;
    private double minimumBrokerage;
    private double maximumBrokerage;
}
