package com.thiru.investment_tracker.common;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "from")
public class ProfitAndLossContext {

    private double purchasePrice;
    private Date purchaseDate;
    private double sellPrice;
    private long sellQuantity;
    private Date sellDate;
}
