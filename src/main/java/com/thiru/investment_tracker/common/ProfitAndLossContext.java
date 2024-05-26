package com.thiru.investment_tracker.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor(staticName = "from")
public class ProfitAndLossContext {

    private double purchasePrice;
    private Date purchaseDate;
    private double sellPrice;
    private long sellQuantity;
    private Date sellDate;
}
