package com.thiru.wealthlens.dto.request;


import com.thiru.wealthlens.dto.enums.AmcChargeFrequency;
import com.thiru.wealthlens.dto.helper.BrokerageChargesDto;
import com.thiru.wealthlens.dto.enums.BrokerName;
import com.thiru.wealthlens.shared.dto.enums.EntityStatus;
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
    private double amcChargesAnnually;
    private AmcChargeFrequency amcChargesFrequency;
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
