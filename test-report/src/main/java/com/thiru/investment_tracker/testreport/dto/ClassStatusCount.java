package com.thiru.investment_tracker.testreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassStatusCount {
    private int passed;
    private int failed;
    private int skipped;
    private int error;
}
