package com.thiru.investment_tracker.service.parser.model;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.InputRecord;
import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.dto.enums.ExcelDataType;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.transaction.TransactionParser;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractRequestParser<RequestType> implements RequestParser<RequestType> {

    protected abstract RequestType toRequest(InputRecord inputRecord);
    protected abstract Map<String, ExcelDataType> simpleDataTypeMap();
    @Override
    public List<RequestType> parse(MultipartFile file) {
        // Excel parser
        ExcelParser excelParser = new ExcelParserImpl();
        InputRecords parsedRecords = excelParser.parse(file, this.simpleDataTypeMap());
        return parseRequests(parsedRecords);
    }

    private List<RequestType> parseRequests(InputRecords records) {
        return TCollectionUtil.map(sanitizeRecords(records), this::toRequest);
    }

    private static List<InputRecord> sanitizeRecords(InputRecords records) {
        List<InputRecord> inputRecords = records.getRecords();
        return TCollectionUtil.filter(inputRecords, filterOutRecord());
    }

    private static Predicate<InputRecord> filterOutRecord() {
        return inputRecord -> inputRecord != null && inputRecord.getRecord() != null;
    }
}
