package com.thiru.investment_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.common.Enums.AssetType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransactionResponse {
    private String email;
    private String stockCode;
    private String stockName;
    private String exchangeName;
    private String brokerName;
    private double price;
    private Long quantity;
    private double totalValue;
    private String actorName;
    private AssetType assetType;
    private Date maturityDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date transactionDate;
}