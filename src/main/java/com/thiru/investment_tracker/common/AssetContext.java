package com.thiru.investment_tracker.common;

import java.util.Date;

import lombok.Data;

@Data
public class AssetContext {
    private double purchasePrice;
    private Date purchaseDate;
    private double sellPrice;
    private long sellQuantity;
    private Date sellDate;

    public static AssetContext from(double purchasePrice, Date purchaseDate, double sellPrice, long sellQuantity, Date sellDate) {
        AssetContext assetContext = new AssetContext();
        assetContext.setPurchasePrice(purchasePrice);
        assetContext.setPurchaseDate(purchaseDate);
        assetContext.setSellPrice(sellPrice);
        assetContext.setSellQuantity(sellQuantity);
        assetContext.setSellDate(sellDate);
        return assetContext;
    }
}
