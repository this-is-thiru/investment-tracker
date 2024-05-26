package com.thiru.investment_tracker.dto;

import com.thiru.investment_tracker.operation.Filter;
import lombok.Data;

import java.util.List;

@Data
public class BulkGetRequest {
    private DateRange dateRange;
    private List<Filter> filters;
}
