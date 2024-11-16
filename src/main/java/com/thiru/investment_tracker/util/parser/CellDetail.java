package com.thiru.investment_tracker.util.parser;

import com.thiru.investment_tracker.dto.enums.ParserDataType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class CellDetail {

    private ParserDataType parserDataType;
    private Object cellValue;

    public static CellDetail def() {
        return CellDetail.of(ParserDataType.NULL, null);
    }
}
