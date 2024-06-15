package com.thiru.investment_tracker.dto;

import java.util.Map;

import lombok.Data;

@Data
public class InputRecord {

    private Map<String, String> record;
    private Integer recordNumber;
}
