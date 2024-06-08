package com.thiru.investment_tracker.common;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "from")
@NoArgsConstructor(staticName = "empty")
public class AssetContext {
    private double purchasePrice;
    private Date purchaseDate;
    private double sellPrice;
    private long sellQuantity;
    private Date sellDate;
}
