package com.thiru.wealthlens.service.parser.model;

import com.thiru.wealthlens.dto.InputRecords;
import com.thiru.wealthlens.dto.enums.ExcelDataType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ExcelParser {
    InputRecords parse(MultipartFile file, Map<String, ExcelDataType> dataTypeMap, List<String> errors);
}
