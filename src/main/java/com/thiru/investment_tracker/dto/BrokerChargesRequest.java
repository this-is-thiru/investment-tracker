package com.thiru.investment_tracker.dto;


import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.EntityStatus;
import com.thiru.investment_tracker.entity.model.BrokerageCharges;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerChargesRequest{
    private BrokerName brokerName;
    private LocalDate startDate;
    private EntityStatus status;
    private double accountOpeningCharges;
    private double amcCharges;
    private BrokerageChargesDto brokerageCharges;
    private double dpChargesPerScrip;
    private double stt;
    private double sebiCharges;
    private double stampDuty;

    /**
     * Example of gst applicable description
     * 18%-brokerage,18%-dp_charges,18%-stt,18%-amc_charges,18%-dp_charges_per_scrip
     */
    private String gstApplicableDescription;
}
