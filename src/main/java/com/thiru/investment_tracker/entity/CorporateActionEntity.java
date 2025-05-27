package com.thiru.investment_tracker.entity;

import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@NoArgsConstructor
@Document(value = "corporate_action")
public class CorporateActionEntity {
    @MongoId
    private String id;
    @Field("stock_code")
    private String stockCode;
    @Field("stock_name")
    private String stockName;
    @Field("to_stock_code")
    private String toStockCode;
    @Field("to_stock_name")
    private String toStockName;
    @Field(name = "type", targetType = FieldType.STRING)
    private CorporateActionType type;
    @Field("description")
    private String description;
    @Field("record_date")
    private String recordDate;
    @Field("ex_date")
    private String exDate;
    @Field("action_price")
    private String actionPrice;
    @Field("ratio")
    private String ratio;
    @Field("date")
    private String date;
}
