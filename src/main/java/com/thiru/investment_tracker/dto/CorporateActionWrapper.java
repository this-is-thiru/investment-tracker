package com.thiru.investment_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.dto.enums.CorporateAction;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CorporateActionWrapper {
    private CorporateAction action;
    private String stockCode;
    private String splitRatio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
    private LocalDate recordDate;
}
