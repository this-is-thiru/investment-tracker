package com.thiru.investment_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.common.Enums.AssetType;
import com.thiru.investment_tracker.common.Enums.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AssetRequest {
    private String email;
    private String stockCode;
    private String stockName;
    private String exchangeName;
    private String brokerName;
    private AssetType assetType;
    private Date maturityDate;
    private double price;
    private Long quantity;
    private TransactionType transactionType;
    private String actorName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date transactionDate;

}
