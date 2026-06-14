package com.thiru.investment_tracker.testreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestClassSummary {
    private String fullyQualifiedClassName;
    private String packageName;
    private String simpleClassName;
    private int totalTests;
    private int passedCount;
    private int failedCount;
    private int errorCount;
    private int skippedCount;
    private double totalTimeSeconds;
    private List<TestCaseResult> testCases;
}