package com.thiru.wealthlens.dto.request;

import com.thiru.wealthlens.dto.enums.AmcChargeFrequency;
import com.thiru.wealthlens.portfolio.dto.enums.BrokerName;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetManagementDetailsRequest {
    private String email;
    private String dematAccountId;
    private BrokerName brokerName;
    private double accountOpeningCharges;
    private double taxOnAccountOpeningCharges;
    private LocalDate lastAmcChargesDeductedOn;
    private AmcChargeFrequency amcChargesFrequency;
}
