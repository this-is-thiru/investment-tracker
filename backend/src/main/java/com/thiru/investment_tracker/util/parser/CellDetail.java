package com.thiru.investment_tracker.util.parser;

import com.thiru.investment_tracker.dto.enums.ExcelDataType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class CellDetail {

    private ExcelDataType excelDataType;
    private Object cellValue;

    public static CellDetail def() {
        return CellDetail.of(ExcelDataType.NULL, null);
    }
}
