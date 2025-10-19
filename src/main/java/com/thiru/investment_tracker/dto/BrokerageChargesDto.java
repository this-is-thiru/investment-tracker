package com.thiru.investment_tracker.dto;


import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.BrokerageAggregatorType;
import com.thiru.investment_tracker.dto.enums.EntityStatus;
import com.thiru.investment_tracker.entity.model.BrokerageCharges;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

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
