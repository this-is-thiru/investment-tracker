package com.thiru.investment_tracker.dto;

import java.util.List;

import lombok.Data;

@Data
public class InputRecords {

    private List<String> headers;
    private List<InputRecord> records;
}
