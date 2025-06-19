package com.thiru.investment_tracker.service.export.writer.model;

import com.thiru.investment_tracker.dto.enums.ExcelDataType;

public record ExcelDataTypePair(Object data, ExcelDataType excelDataType) {
}
