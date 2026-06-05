package com.thiru.investment_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedriveResult {
    private List<String> succeeded;
    private Map<String, String> failed;
    private List<String> stillFiltered;
    private List<String> filteredOut;
    private String message;
}