package com.thiru.investment_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.time.TLocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CorporateActionDto {
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


    @CreatedBy
    private String createdBy;
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    private LocalDateTime createdAt;
}
