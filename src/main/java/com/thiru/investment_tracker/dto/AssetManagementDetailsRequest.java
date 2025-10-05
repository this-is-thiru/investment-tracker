package com.thiru.investment_tracker.dto;

import com.thiru.investment_tracker.dto.enums.AmcChargeFrequency;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetManagementDetailsRequest {
    private String email;
    private String dematAccountId;
    private BrokerName brokerName;
    private double accountOpeningCharges;
    private double taxOnAccountOpeningCharges;
    private double amcCharges;
    private double taxOnAmcCharges;
    private LocalDate lastAmcChargesDeductedOn;
    private AmcChargeFrequency amcChargesFrequency;
}
