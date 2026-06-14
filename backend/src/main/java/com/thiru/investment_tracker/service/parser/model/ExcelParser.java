package com.thiru.investment_tracker.service.parser.model;

import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.dto.enums.ExcelDataType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ExcelParser {
    InputRecords parse(MultipartFile file, Map<String, ExcelDataType> dataTypeMap, List<String> errors);
}
