package com.thiru.investment_tracker.entity.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

// TODO: Rename the document fields purchasePrice -> purchaseAmount, sellPrice -> sellAmount, brokerCharges -> brokerage
@Data
@NoArgsConstructor(staticName = "from")
@EqualsAndHashCode(callSuper = true)
public class FortnightReport extends ReportModel {
//	private double purchaseAmount;
//	private double sellAmount;
//	private double profit;
//	private double brokerCharges;
//	private double miscCharges;
//
//    private double brokerage;
//    private double accountOpeningCharges;
//    private double amcCharges;
//    private double govtCharges;
//    private double taxes;
//    private double dpCharges;
}
