package com.thiru.wealthlens.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ReportModelResponse {
    private double purchasePrice;
    private double sellPrice;
    private double profit;
    private double brokerCharges;
    private double miscCharges;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdatedTime;
}
