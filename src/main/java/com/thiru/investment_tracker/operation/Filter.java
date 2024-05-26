package com.thiru.investment_tracker.operation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Filter {

    private String filterKey;
    private FilterOperation operation;
    private Object value;
    private List<Object> values = new ArrayList<>();
    private LogicalOperation logicalOperation;
    private LogicalOperation expressionLogicalOperation;
    private Boolean allowEmptyOrNull= false;
    private Boolean caseSensitive = false;

    public enum FilterOperation {
        EQUALS,
        NOT_EQUALS,
        STARTS_WITH,
        CONTAINS,
        GREATER_THAN,
        LESSER_THAN,
        GREATER_THAN_OR_EQUAL_TO,
        LESSER_THAN_OR_EQUAL_TO,
        IS_NULL,
        IS_NOT_NULL
    }

    public enum LogicalOperation {
        AND,
        OR
    }
}
