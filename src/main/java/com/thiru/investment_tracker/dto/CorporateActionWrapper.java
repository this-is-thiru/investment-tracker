package com.thiru.investment_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CorporateActionWrapper {
    private String stockCode;
    private String stockName;
    private String toStockCode;
    private String toStockName;
    private CorporateActionType type;
    private String description;
    private String actionPrice;
    private String ratio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
    private LocalDate exDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
    private LocalDate recordDate;
}
