package com.thiru.wealthlens.portfolio.service.parser.model;

import com.thiru.wealthlens.shared.dto.InputRecords;
import com.thiru.wealthlens.shared.dto.enums.ExcelDataType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ExcelParser {
    InputRecords parse(MultipartFile file, Map<String, ExcelDataType> dataTypeMap, List<String> errors);
}
