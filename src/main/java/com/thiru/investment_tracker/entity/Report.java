package com.thiru.investment_tracker.entity;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.common.CommonUtil;
import com.thiru.investment_tracker.common.Enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document(value = "reports")
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class Report {
    @JsonIgnore
    @MongoId
    private String id;

    @Field("email")
    private String email;

    @Field("stock_code")
    private String stockCode;

    @Field("stock_name")
    private String stockName;

    @Field("exchange_name")
    private String exchangeName;

    @Field("broker_name")
    private String brokerName;

    @Field("purchase_price")
    private double purchasePrice;

    @Field("sell_price")
    private double sellPrice;

    @Field("quantity")
    private Long quantity;

    @Field("total_value")
    private double totalValue;

    @Field(name = "asset_type", targetType = FieldType.STRING)
    private Enums.AssetType assetType;

    @Field("actor_name")
    private String actorName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonUtil.DATE_FORMAT)
    @Field("purchase_date")
    private Date purchaseDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonUtil.DATE_FORMAT)
    @Field("sell_date")
    private Date sellDate;

}